package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Transactions;
import com.chokoreto.client.ui.ClientScreen;
import com.chokoreto.client.ui.HudEditorScreen;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** General settings and reset buttons. */
public class SettingsPage extends Page {
    @Override
    public void build() {
        ws.clear();
        Config c = Config.get();

        ws.add(new W.Toggle(x + 8, y + 34, 220, 16, "Announce auctions in chat by default",
                () -> c.announceDefault, v -> c.announceDefault = v));
        ws.add(new W.Toggle(x + 8, y + 56, 220, 16, "Debug: show unrecognised payment lines",
                Transactions::isDebug, Transactions::setDebug));

        ws.add(new W.Btn(x + 8, y + 94, 110, 16, "Edit HUD positions",
                () -> Minecraft.getInstance().gui.setScreen(new HudEditorScreen(s))));
        ws.add(new W.Btn(x + 124, y + 94, 110, 16, "Reset HUD positions", () -> {
            c.resetHudPositions();
            Config.save();
        }));
        ws.add(new W.Btn(x + 8, y + 116, 110, 16, "Reset all settings", () -> {
            Config.reset();
            Minecraft.getInstance().gui.setScreen(new ClientScreen(ClientScreen.Tab.SETTINGS));
        }));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, "Settings", "Everything is saved automatically.");
        card(g, x, y + 26, w, 52);
        card(g, x, y + 86, w, 52);

        Ui.text(g, "Chokoreto Client 1.0 - Minecraft 26.2", x + 8, y + 152, Ui.TEXT);
        Ui.text(g, "Open the menu with Right Shift (change it in Controls) or /choko.", x + 8, y + 166, Ui.MUTED);
        Ui.text(g, "Commands: /choko, /choko test <name> <amount>, /choko cancel", x + 8, y + 178, Ui.MUTED);
    }
}
