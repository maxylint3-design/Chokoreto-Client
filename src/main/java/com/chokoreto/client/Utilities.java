package com.chokoreto.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** The data behind the utility HUD boxes: ping, item counts, nearby players and timers. */
public final class Utilities {
    private Utilities() {}

    // ---------------------------------------------------------------- nearby players

    public static final class Nearby {
        public final String name;
        public final int distance;

        Nearby(String name, int distance) {
            this.name = name;
            this.distance = distance;
        }
    }

    private static final Map<String, Long> LAST_ALERT = new HashMap<>();
    private static List<Nearby> nearby = new ArrayList<>();

    public static List<Nearby> nearby() {
        return nearby;
    }

    /** Called 20 times a second. */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        Config c = Config.get();

        // ---- nearby players ----
        List<Nearby> found = new ArrayList<>();
        if (mc.player != null && mc.level != null && (c.nearbyShow || c.nearbyAlert)) {
            for (AbstractClientPlayer p : mc.level.players()) {
                if (p == mc.player) continue;
                int dist = (int) p.distanceTo(mc.player);
                if (dist <= c.nearbyRadius) found.add(new Nearby(p.getName().getString(), dist));
            }
            found.sort((a, b) -> Integer.compare(a.distance, b.distance));

            if (c.nearbyAlert) {
                long now = System.currentTimeMillis();
                for (Nearby n : found) {
                    Long last = LAST_ALERT.get(n.name);
                    if (last == null || now - last > 60_000) {
                        AuctionManager.info(n.name + " is nearby (" + n.distance + " blocks)");
                    }
                    LAST_ALERT.put(n.name, now);
                }
            }
        }
        nearby = found;

        // ---- timers ----
        long now = System.currentTimeMillis();
        Iterator<Store.TimerEntry> it = Store.get().timers.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            Store.TimerEntry t = it.next();
            if (now >= t.endAt) {
                AuctionManager.info("Timer finished: " + t.label);
                it.remove();
                changed = true;
            }
        }
        if (changed) Store.save();
    }

    public static void addTimer(String label, long minutes) {
        Store.TimerEntry t = new Store.TimerEntry();
        t.label = label.isBlank() ? "Timer" : label.trim();
        t.endAt = System.currentTimeMillis() + minutes * 60_000L;
        Store.get().timers.add(t);
        Store.save();
    }

    // ---------------------------------------------------------------- misc info

    /** Your ping in ms, or -1 if unknown. */
    public static int ping() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) return -1;
        var info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return info == null ? -1 : info.getLatency();
    }

    public static int count(Item... items) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        int total = 0;
        var inv = mc.player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            for (Item item : items) {
                if (stack.getItem() == item) total += stack.getCount();
            }
        }
        return total;
    }

    public static int totems() {
        return count(Items.TOTEM_OF_UNDYING);
    }

    public static int crystals() {
        return count(Items.END_CRYSTAL);
    }

    public static int gapples() {
        return count(Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE);
    }

    public static String direction() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return "?";
        float yaw = mc.player.getYRot() % 360f;
        if (yaw < 0) yaw += 360f;
        String[] names = {"S", "SW", "W", "NW", "N", "NE", "E", "SE"};
        return names[Math.round(yaw / 45f) % 8];
    }
}
