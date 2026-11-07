package com.chokoreto.client;

import com.chokoreto.client.ui.ClientScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Chokoreto Client - entry point. Fabric calls onInitializeClient() once when the game starts,
 * and this is where every part of the mod gets hooked up.
 */
public class ChokoretoClient implements ClientModInitializer {
    public static final String MOD_ID = "chokoreto";

    private static KeyMapping openKey;

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitializeClient() {
        Config.load();
        Store.load();

        // 1) Key to open the menu (default Right Shift - change it in Options > Controls)
        KeyMapping.Category category = KeyMapping.Category.register(id("main"));
        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.chokoreto.open",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_RSHIFT,
                category));

        // 2) Runs 20 times per second
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openKey.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    client.gui.setScreen(new ClientScreen());
                }
            }
            AuctionManager.tick();
            Utilities.tick();
        });

        // 3) Every system chat line goes to the transaction tracker
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) Transactions.onLine(message.getString());
        });

        // 4) The HUD boxes (auction, profit, info, nearby players, timers)
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("hud"), Hud::render);

        // 5) Custom crosshair: replaces the vanilla one when a custom style is selected
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
            if (Crosshair.isCustom()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.gui.screen() == null) {
                    Crosshair.draw(graphics, mc.getWindow().getGuiScaledWidth() / 2, mc.getWindow().getGuiScaledHeight() / 2);
                }
            } else {
                original.extractRenderState(graphics, deltaTracker);
            }
        });

        // 6) Scoreboard: can be hidden from the Scoreboard tab
        HudElementRegistry.replaceElement(VanillaHudElements.SCOREBOARD, original -> (graphics, deltaTracker) -> {
            if (!Config.get().hideScoreboard) original.extractRenderState(graphics, deltaTracker);
        });

        // 7) Commands
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) ->
                dispatcher.register(ClientCommands.literal("choko")
                        // /choko -> open the menu
                        .executes(context -> {
                            Minecraft mc = Minecraft.getInstance();
                            mc.execute(() -> mc.gui.setScreen(new ClientScreen()));
                            return 1;
                        })
                        // /choko cancel -> cancel the running auction
                        .then(ClientCommands.literal("cancel").executes(context -> {
                            AuctionManager.cancel();
                            return 1;
                        }))
                        // /choko debug -> show chat lines that mention "paid" but were not understood
                        .then(ClientCommands.literal("debug").executes(context -> {
                            Transactions.setDebug(!Transactions.isDebug());
                            AuctionManager.info("Debug is now " + (Transactions.isDebug() ? "ON" : "OFF"));
                            return 1;
                        }))
                        // /choko test <name> <amount> -> pretend someone paid you (nothing real happens)
                        .then(ClientCommands.literal("test")
                                .then(ClientCommands.argument("name", StringArgumentType.word())
                                        .then(ClientCommands.argument("amount", StringArgumentType.word())
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "name");
                                                    String amountText = StringArgumentType.getString(context, "amount");
                                                    long amount = Format.parseMoney(amountText);
                                                    if (amount <= 0) {
                                                        AuctionManager.info("Could not read the amount '" + amountText + "'. Try 5m or 500k.");
                                                    } else {
                                                        Transactions.record(name, amount, true);
                                                    }
                                                    return 1;
                                                }))))));
    }
}
