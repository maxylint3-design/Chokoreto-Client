package com.chokoreto.client;

import com.chokoreto.client.ui.Ui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

/**
 * All the boxes drawn on your screen: auction, profit, info panel, nearby players, timers.
 * Each box is an {@link Element}. The positions are saved in the config and can be dragged
 * around in the HUD editor.
 */
public final class Hud {
    private Hud() {}

    public record Line(String text, int color) {}

    public record Content(ItemStack icon, List<Line> lines, boolean skin) {}

    public interface Element {
        String name();

        Config.Pos pos();

        boolean enabled();

        /** What to draw. Return null to draw nothing. {@code preview} = true in the HUD editor (show sample data). */
        Content content(boolean preview);
    }

    public static final List<Element> ELEMENTS = new ArrayList<>();

    static {
        ELEMENTS.add(new Element() {
            public String name() { return "Auction"; }
            public Config.Pos pos() { return Config.get().auctionPos; }
            public boolean enabled() { return true; }
            public Content content(boolean preview) { return auction(preview); }
        });
        ELEMENTS.add(new Element() {
            public String name() { return "Profit"; }
            public Config.Pos pos() { return Config.get().profitPos; }
            public boolean enabled() { return Config.get().profitShow; }
            public Content content(boolean preview) { return profit(preview); }
        });
        ELEMENTS.add(new Element() {
            public String name() { return "Info panel"; }
            public Config.Pos pos() { return Config.get().infoPos; }
            public boolean enabled() { return Config.get().infoShow; }
            public Content content(boolean preview) { return info(preview); }
        });
        ELEMENTS.add(new Element() {
            public String name() { return "Nearby players"; }
            public Config.Pos pos() { return Config.get().nearbyPos; }
            public boolean enabled() { return Config.get().nearbyShow; }
            public Content content(boolean preview) { return nearby(preview); }
        });
        ELEMENTS.add(new Element() {
            public String name() { return "Timers"; }
            public Config.Pos pos() { return Config.get().timersPos; }
            public boolean enabled() { return Config.get().timersShow; }
            public Content content(boolean preview) { return timers(preview); }
        });
    }

    // ------------------------------------------------------------ content builders

    private static Content auction(boolean preview) {
        Auction a = AuctionManager.get();
        if (a == null) {
            if (!preview) return null;
            List<Line> l = new ArrayList<>();
            l.add(new Line("1x Elytra", Ui.TEXT));
            l.add(new Line("Highest: $5m by Steve", Ui.accent()));
            l.add(new Line("0:42 left", Ui.MUTED));
            return new Content(new ItemStack(Items.ELYTRA), l, true);
        }
        List<Line> l = new ArrayList<>();
        l.add(new Line(a.title(), Ui.TEXT));
        l.add(new Line(a.highestBidder == null
                ? "No bids yet (min " + Format.money(a.minBid) + ")"
                : "Highest: " + Format.money(a.highestBid) + " by " + a.highestBidder, Ui.accent()));
        l.add(new Line(Format.clock(a.millisLeft()) + " left", Ui.MUTED));
        return new Content(a.stack, l, true);
    }

    private static Content profit(boolean preview) {
        long net = preview ? 12_500_000L : Transactions.sessionNet();
        List<Line> l = new ArrayList<>();
        l.add(new Line(Format.signed(net), net < 0 ? Ui.RED : Ui.GREEN));
        l.add(new Line("Session profit", Ui.MUTED));
        return new Content(null, l, Config.get().profitSkin);
    }

    private static Content info(boolean preview) {
        Config c = Config.get();
        List<Line> l = new ArrayList<>();
        if (c.infoFps) l.add(new Line("FPS  " + fps, Ui.TEXT));
        if (c.infoPing) {
            int ping = Utilities.ping();
            l.add(new Line("Ping  " + (ping < 0 ? "-" : ping + "ms"), Ui.TEXT));
        }
        Minecraft mc = Minecraft.getInstance();
        if (c.infoCoords && mc.player != null) {
            l.add(new Line("XYZ  " + (int) Math.floor(mc.player.getX()) + " "
                    + (int) Math.floor(mc.player.getY()) + " " + (int) Math.floor(mc.player.getZ()), Ui.TEXT));
        }
        if (c.infoDirection) l.add(new Line("Facing  " + Utilities.direction(), Ui.TEXT));
        if (c.infoTotems) l.add(new Line("Totems  " + Utilities.totems(), Ui.TEXT));
        if (c.infoCrystals) l.add(new Line("Crystals  " + Utilities.crystals(), Ui.TEXT));
        if (c.infoGapples) l.add(new Line("Gapples  " + Utilities.gapples(), Ui.TEXT));
        if (l.isEmpty()) return preview ? new Content(null, List.of(new Line("Info panel", Ui.MUTED)), true) : null;
        return new Content(null, l, true);
    }

    private static Content nearby(boolean preview) {
        List<Utilities.Nearby> list = Utilities.nearby();
        List<Line> l = new ArrayList<>();
        if (list.isEmpty()) {
            if (!preview) return null;
            l.add(new Line("Nearby (2)", Ui.accent()));
            l.add(new Line("Steve  23m", Ui.TEXT));
            l.add(new Line("Alex  41m", Ui.TEXT));
            return new Content(null, l, true);
        }
        l.add(new Line("Nearby (" + list.size() + ")", Ui.accent()));
        int shown = 0;
        for (Utilities.Nearby n : list) {
            if (shown++ >= 6) break;
            l.add(new Line(n.name + "  " + n.distance + "m", Ui.TEXT));
        }
        return new Content(null, l, true);
    }

    private static Content timers(boolean preview) {
        List<Store.TimerEntry> list = Store.get().timers;
        List<Line> l = new ArrayList<>();
        if (list.isEmpty()) {
            if (!preview) return null;
            l.add(new Line("Timers", Ui.accent()));
            l.add(new Line("Drill  4:12", Ui.TEXT));
            return new Content(null, l, true);
        }
        l.add(new Line("Timers", Ui.accent()));
        long now = System.currentTimeMillis();
        int shown = 0;
        for (Store.TimerEntry t : list) {
            if (shown++ >= 6) break;
            l.add(new Line(t.label + "  " + Format.clock(t.endAt - now), Ui.TEXT));
        }
        return new Content(null, l, true);
    }

    // ------------------------------------------------------------ measuring and drawing

    private static final int PAD = 6;
    private static final int LINE_H = 11;

    public static int[] measure(Font font, Content c) {
        int textWidth = 0;
        for (Line line : c.lines()) textWidth = Math.max(textWidth, font.width(line.text()));
        boolean icon = c.icon() != null && !c.icon().isEmpty();
        int w = PAD + (icon ? 38 : 0) + textWidth + PAD;
        int h = Math.max(icon ? 44 : 0, c.lines().size() * LINE_H + PAD * 2 - 3);
        if (!c.skin()) {
            w = textWidth;
            h = c.lines().size() * LINE_H;
        }
        return new int[]{w, h};
    }

    /** Works out where on screen an element is drawn (handles the "stick to right edge" x &lt; 0). */
    public static int[] place(Config.Pos pos, int w, int h, int screenW, int screenH) {
        int x = pos.x < 0 ? screenW - w - 6 : pos.x;
        int y = pos.y;
        x = Math.max(0, Math.min(x, Math.max(0, screenW - w)));
        y = Math.max(0, Math.min(y, Math.max(0, screenH - h)));
        return new int[]{x, y};
    }

    public static void draw(GuiGraphicsExtractor g, Font font, Content c, int x, int y) {
        Config cfg = Config.get();
        int[] size = measure(font, c);
        int w = size[0];
        int h = size[1];
        boolean icon = c.icon() != null && !c.icon().isEmpty();
        int textX;
        int textY;

        if (c.skin()) {
            Ui.round(g, x, y, w, h, Ui.pct(Ui.BG, cfg.hudOpacity));
            g.fill(x, y + 1, x + 2, y + h - 1, Ui.accent());
            textX = x + PAD + (icon ? 38 : 0);
            textY = y + (h - c.lines().size() * LINE_H + 2) / 2;
            if (icon) {
                Matrix3x2fStack pose = g.pose();
                pose.pushMatrix();
                pose.translate(x + PAD + 2, y + (h - 32) / 2f);
                pose.scale(2f, 2f);
                g.item(c.icon(), 0, 0);
                pose.popMatrix();
            }
        } else {
            textX = x;
            textY = y;
        }

        for (Line line : c.lines()) {
            g.text(font, line.text(), textX, textY, line.color() | 0xFF000000, cfg.textShadow);
            textY += LINE_H;
        }
    }

    // ------------------------------------------------------------ the main render

    private static int fps = 0;
    private static int frames = 0;
    private static long lastFpsTime = System.currentTimeMillis();

    /** Called once per frame by Fabric. Draws every enabled box. */
    public static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();

        // count frames to work out FPS
        frames++;
        long now = System.currentTimeMillis();
        if (now - lastFpsTime >= 1000) {
            fps = frames;
            frames = 0;
            lastFpsTime = now;
        }

        if (mc.player == null) return;
        if (mc.gui.screen() instanceof com.chokoreto.client.ui.HudEditorScreen) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();
        Font font = mc.font;

        for (Element e : ELEMENTS) {
            if (!e.enabled()) continue;
            Content c = e.content(false);
            if (c == null) continue;
            int[] size = measure(font, c);
            int[] at = place(e.pos(), size[0], size[1], sw, sh);
            draw(g, font, c, at[0], at[1]);
        }
    }
}
