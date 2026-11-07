package com.chokoreto.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Saved data (auction history, transactions, locker, timers).
 * Saved to  .minecraft/config/chokoreto/data.json
 */
public final class Store {
    public static final class AuctionRecord {
        public String itemId;
        public String name;
        public int count;
        public long minBid;
        public long price;      // 0 = no bids
        public String winner;   // null = no bids
        public long endedAt;
    }

    public static final class Tx {
        public String player;
        public long amount;
        public boolean received;
        public long time;
    }

    public static final class LockerItem {
        public String itemId;
        public int count = 1;
        public long minBid = 1_000_000L;
    }

    public static final class TimerEntry {
        public String label;
        public long endAt;
    }

    public List<AuctionRecord> auctions = new ArrayList<>();
    public List<Tx> txs = new ArrayList<>();
    public List<LockerItem> locker = new ArrayList<>();
    public List<TimerEntry> timers = new ArrayList<>();
    public long sessionStart = System.currentTimeMillis();
    public long sessionReceived = 0;
    public long sessionSent = 0;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Store instance;

    public static Store get() {
        if (instance == null) load();
        return instance;
    }

    public static void load() {
        Path file = Config.folder().resolve("data.json");
        Store loaded = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                loaded = GSON.fromJson(reader, Store.class);
            } catch (Exception e) {
                System.err.println("[Chokoreto] Could not read data, starting fresh: " + e);
            }
        }
        instance = loaded != null ? loaded : new Store();
        if (instance.auctions == null) instance.auctions = new ArrayList<>();
        if (instance.txs == null) instance.txs = new ArrayList<>();
        if (instance.locker == null) instance.locker = new ArrayList<>();
        if (instance.timers == null) instance.timers = new ArrayList<>();
    }

    public static void save() {
        Path file = Config.folder().resolve("data.json");
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(get(), writer);
        } catch (IOException e) {
            System.err.println("[Chokoreto] Could not save data: " + e);
        }
    }
}
