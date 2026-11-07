package com.chokoreto.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * All the settings. Saved to  .minecraft/config/chokoreto/config.json  so they survive restarts.
 * Every public field here is a setting - add a field and it is saved automatically.
 */
public final class Config {
    /** A position on screen. x or y below 0 means "stick to the right edge" (x only). */
    public static final class Pos {
        public int x;
        public int y;

        public Pos() {}

        public Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // ---------------- theme ----------------
    public int accent = 0xFFE0A96D;       // caramel
    public int panelOpacity = 92;         // percent, menu background
    public int hudOpacity = 70;           // percent, HUD boxes
    public boolean textShadow = true;

    // ---------------- auction ----------------
    public boolean announceDefault = true;

    // ---------------- transactions / profit ----------------
    public boolean profitShow = true;
    public boolean profitSkin = true;

    // ---------------- utilities ----------------
    public boolean infoShow = true;
    public boolean infoFps = true;
    public boolean infoPing = true;
    public boolean infoCoords = true;
    public boolean infoDirection = true;
    public boolean infoTotems = true;
    public boolean infoCrystals = true;
    public boolean infoGapples = true;

    public boolean nearbyShow = true;
    public boolean nearbyAlert = true;
    public int nearbyRadius = 64;

    public boolean timersShow = true;

    // ---------------- crosshair ----------------
    /** VANILLA, CROSS, DOT, CROSS_DOT, CIRCLE, CIRCLE_DOT, T */
    public String crosshairStyle = "VANILLA";
    public int crosshairColor = 0xFFFFFFFF;
    public int crosshairSize = 6;
    public int crosshairThickness = 1;
    public int crosshairGap = 2;
    public boolean crosshairOutline = true;

    // ---------------- scoreboard ----------------
    public boolean hideScoreboard = false;

    // ---------------- music ----------------
    public int musicVolume = 60;

    // ---------------- HUD positions ----------------
    public Pos auctionPos = new Pos(6, 6);
    public Pos profitPos = new Pos(6, 56);
    public Pos infoPos = new Pos(6, 88);
    public Pos nearbyPos = new Pos(-1, 60);
    public Pos timersPos = new Pos(-1, 6);

    public void resetHudPositions() {
        auctionPos = new Pos(6, 6);
        profitPos = new Pos(6, 56);
        infoPos = new Pos(6, 88);
        nearbyPos = new Pos(-1, 60);
        timersPos = new Pos(-1, 6);
    }

    // ------------------------------------------------------------ loading / saving

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Config instance;

    public static Config get() {
        if (instance == null) load();
        return instance;
    }

    public static Path folder() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("chokoreto");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {
        }
        return dir;
    }

    public static void load() {
        Path file = folder().resolve("config.json");
        Config loaded = null;
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                loaded = GSON.fromJson(reader, Config.class);
            } catch (Exception e) {
                System.err.println("[Chokoreto] Could not read config, using defaults: " + e);
            }
        }
        instance = loaded != null ? loaded : new Config();
        // make sure nothing is null after loading an older file
        if (instance.auctionPos == null) instance.auctionPos = new Pos(6, 6);
        if (instance.profitPos == null) instance.profitPos = new Pos(6, 56);
        if (instance.infoPos == null) instance.infoPos = new Pos(6, 88);
        if (instance.nearbyPos == null) instance.nearbyPos = new Pos(-1, 60);
        if (instance.timersPos == null) instance.timersPos = new Pos(-1, 6);
        if (instance.crosshairStyle == null) instance.crosshairStyle = "VANILLA";
    }

    public static void save() {
        Path file = folder().resolve("config.json");
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(get(), writer);
        } catch (IOException e) {
            System.err.println("[Chokoreto] Could not save config: " + e);
        }
    }

    public static void reset() {
        instance = new Config();
        save();
    }
}
