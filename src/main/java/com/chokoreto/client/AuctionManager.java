package com.chokoreto.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * Runs the auction: starts it, decides who the highest bidder is, ends it when the time is up.
 * Payments arrive from {@link Transactions} (it reads the chat).
 */
public final class AuctionManager {
    private AuctionManager() {}

    private static final long KEEP_AFTER_END_MS = 60_000;

    private static Auction current;

    public static Auction get() {
        return current;
    }

    public static void start(ItemStack stack, int count, long minBid, long durationSeconds, boolean announce) {
        Minecraft mc = Minecraft.getInstance();
        current = new Auction(stack, count, minBid, durationSeconds, announce);

        info("Auction started: " + current.title() + " | min bid " + Format.money(minBid)
                + " | " + Format.duration(durationSeconds));

        if (announce && mc.player != null) {
            String me = mc.getUser().getName();
            say("AUCTION: " + current.title() + " | Min bid " + Format.money(minBid)
                    + " | " + Format.duration(durationSeconds) + " | /pay " + me + " <amount> to bid");
        }
    }

    public static void cancel() {
        if (current == null) {
            info("There is no auction running.");
            return;
        }
        current = null;
        info("Auction cancelled.");
    }

    /** Called 20 times a second. */
    public static void tick() {
        Auction a = current;
        if (a == null) return;
        long now = System.currentTimeMillis();

        if (!a.finished && now >= a.endTimeMs) {
            a.finished = true;
            a.finishedAtMs = now;
            saveRecord(a);
            if (a.highestBidder == null) {
                info("Auction over: " + a.title() + " got no bids.");
                if (a.announce) say("AUCTION ENDED: " + a.title() + " got no bids.");
            } else {
                info("Auction over! " + a.highestBidder + " won " + a.title() + " for " + Format.money(a.highestBid));
                if (a.announce) say("SOLD! " + a.highestBidder + " won " + a.title() + " for " + Format.money(a.highestBid));
            }
        }

        if (a.finished && now - a.finishedAtMs > KEEP_AFTER_END_MS) {
            current = null;
        }
    }

    private static void saveRecord(Auction a) {
        Store.AuctionRecord r = new Store.AuctionRecord();
        r.itemId = Stacks.idOf(a.stack);
        r.name = a.stack.getHoverName().getString();
        r.count = a.count;
        r.minBid = a.minBid;
        r.price = a.highestBidder == null ? 0 : a.highestBid;
        r.winner = a.highestBidder;
        r.endedAt = System.currentTimeMillis();
        Store store = Store.get();
        store.auctions.add(0, r);
        while (store.auctions.size() > 100) store.auctions.remove(store.auctions.size() - 1);
        Store.save();
    }

    /** Someone paid us. Is it a valid bid? */
    public static void handlePayment(String player, long amount) {
        Auction a = current;
        if (a == null || a.finished) return;

        if (amount < a.minBid) {
            info(player + " paid " + Format.money(amount) + " (below the minimum of " + Format.money(a.minBid) + ", ignored)");
            return;
        }

        if (a.highestBidder == null || amount > a.highestBid) {
            a.highestBid = amount;
            a.highestBidder = player;
            info("New highest bid: " + Format.money(amount) + " by " + player);
            if (a.announce) say("NEW BID: " + Format.money(amount) + " by " + player + " on " + a.title());
        } else {
            info(player + " paid " + Format.money(amount) + " but the highest bid is " + Format.money(a.highestBid));
        }
    }

    /** Message shown only to you. */
    public static void info(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal("[Chokoreto] " + message).withStyle(ChatFormatting.GOLD));
        }
    }

    /** PUBLIC chat message sent as you. */
    private static void say(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.connection != null) {
            mc.player.connection.sendChat(message);
        }
    }
}
