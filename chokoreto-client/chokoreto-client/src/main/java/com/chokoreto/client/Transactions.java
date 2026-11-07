package com.chokoreto.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads server chat for money coming in and going out, keeps the history and the session profit.
 *
 * ====== THE CHAT PATTERNS ======
 * Donut SMP says "Player paid you $5M" when you get paid.
 * When YOU pay, it is assumed to say "You paid Player $5M" - if the real wording is different,
 * edit SENT below (run /choko debug to see the lines the mod could not understand).
 *
 * Both must match the WHOLE line, so other players can't fake a payment by typing it in chat.
 */
public final class Transactions {
    private Transactions() {}

    private static final String MONEY = "\\$([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)";

    private static final Pattern RECEIVED = Pattern.compile(
            "^\\W*([A-Za-z0-9_.]{3,17}) paid you " + MONEY + "\\W*$");

    private static final Pattern SENT = Pattern.compile(
            "^\\W*You paid ([A-Za-z0-9_.]{3,17}) " + MONEY + "\\W*$");

    private static boolean debug = false;

    public static void setDebug(boolean on) {
        debug = on;
    }

    public static boolean isDebug() {
        return debug;
    }

    /** Called for every system chat line. */
    public static void onLine(String line) {
        if (line == null) return;
        String trimmed = line.trim();

        Matcher in = RECEIVED.matcher(trimmed);
        if (in.matches()) {
            long amount = Format.parseMoney(in.group(2) + in.group(3));
            if (amount > 0) record(in.group(1), amount, true);
            return;
        }

        Matcher out = SENT.matcher(trimmed);
        if (out.matches()) {
            long amount = Format.parseMoney(out.group(2) + out.group(3));
            if (amount > 0) record(out.group(1), amount, false);
            return;
        }

        if (debug && trimmed.toLowerCase(Locale.ROOT).contains("paid")) {
            AuctionManager.info("[debug] Line with 'paid' did not match: " + trimmed);
        }
    }

    /** Records a payment. Also used by /choko test. */
    public static void record(String player, long amount, boolean received) {
        Store store = Store.get();

        Store.Tx tx = new Store.Tx();
        tx.player = player;
        tx.amount = amount;
        tx.received = received;
        tx.time = System.currentTimeMillis();
        store.txs.add(0, tx);
        while (store.txs.size() > 1000) store.txs.remove(store.txs.size() - 1);

        if (received) store.sessionReceived += amount;
        else store.sessionSent += amount;
        Store.save();

        if (received) AuctionManager.handlePayment(player, amount);
    }

    public static long totalReceived() {
        long sum = 0;
        for (Store.Tx tx : Store.get().txs) if (tx.received) sum += tx.amount;
        return sum;
    }

    public static long totalSent() {
        long sum = 0;
        for (Store.Tx tx : Store.get().txs) if (!tx.received) sum += tx.amount;
        return sum;
    }

    /** Profit for this session (received minus sent). Negative = you are down. */
    public static long sessionNet() {
        Store s = Store.get();
        return s.sessionReceived - s.sessionSent;
    }

    public static void resetSession() {
        Store s = Store.get();
        s.sessionReceived = 0;
        s.sessionSent = 0;
        s.sessionStart = System.currentTimeMillis();
        Store.save();
    }

    public static void clearHistory() {
        Store.get().txs.clear();
        Store.save();
    }

    /** @param type 0 = all, 1 = received, 2 = sent */
    public static List<Store.Tx> filtered(long minAmount, int type, String playerQuery) {
        List<Store.Tx> result = new ArrayList<>();
        String q = playerQuery == null ? "" : playerQuery.trim().toLowerCase(Locale.ROOT);
        for (Store.Tx tx : Store.get().txs) {
            if (tx.amount < minAmount) continue;
            if (type == 1 && !tx.received) continue;
            if (type == 2 && tx.received) continue;
            if (!q.isEmpty() && !tx.player.toLowerCase(Locale.ROOT).contains(q)) continue;
            result.add(tx);
        }
        return result;
    }
}
