package com.chokoreto.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Draws the custom crosshair styles. Also used by the live preview in the menu. */
public final class Crosshair {
    private Crosshair() {}

    public static final String[] STYLES = {"VANILLA", "CROSS", "DOT", "CROSS_DOT", "CIRCLE", "CIRCLE_DOT", "T"};
    public static final String[] STYLE_NAMES = {"Vanilla", "Cross", "Dot", "Cross + Dot", "Circle", "Circle + Dot", "T-Shape"};

    public static boolean isCustom() {
        return !"VANILLA".equals(Config.get().crosshairStyle);
    }

    /** Draws the crosshair centred on (cx, cy). */
    public static void draw(GuiGraphicsExtractor g, int cx, int cy) {
        Config c = Config.get();
        if (!isCustom()) return;
        if (c.crosshairOutline) shape(g, cx, cy, 0xCC000000, 1);
        shape(g, cx, cy, c.crosshairColor | 0xFF000000, 0);
    }

    private static void shape(GuiGraphicsExtractor g, int cx, int cy, int color, int grow) {
        Config c = Config.get();
        String style = c.crosshairStyle;
        int size = c.crosshairSize;
        int gap = c.crosshairGap;
        int t = Math.max(1, c.crosshairThickness);
        int half = t / 2;

        boolean cross = style.equals("CROSS") || style.equals("CROSS_DOT");
        boolean tShape = style.equals("T");
        boolean dot = style.equals("DOT") || style.equals("CROSS_DOT") || style.equals("CIRCLE_DOT");
        boolean circle = style.equals("CIRCLE") || style.equals("CIRCLE_DOT");

        if (cross || tShape) {
            // left
            g.fill(cx - gap - size - grow, cy - half - grow, cx - gap + grow, cy - half + t + grow, color);
            // right
            g.fill(cx + gap - grow, cy - half - grow, cx + gap + size + grow, cy - half + t + grow, color);
            // down
            g.fill(cx - half - grow, cy + gap - grow, cx - half + t + grow, cy + gap + size + grow, color);
            // up (not for the T shape)
            if (cross) {
                g.fill(cx - half - grow, cy - gap - size - grow, cx - half + t + grow, cy - gap + grow, color);
            }
        }

        if (circle) {
            int radius = size + gap;
            int steps = Math.max(24, (int) (radius * 7));
            for (int i = 0; i < steps; i++) {
                double angle = 2 * Math.PI * i / steps;
                int px = cx + (int) Math.round(Math.cos(angle) * radius);
                int py = cy + (int) Math.round(Math.sin(angle) * radius);
                g.fill(px - half - grow, py - half - grow, px - half + t + grow, py - half + t + grow, color);
            }
        }

        if (dot) {
            int d = Math.max(2, t);
            g.fill(cx - d / 2 - grow, cy - d / 2 - grow, cx - d / 2 + d + grow, cy - d / 2 + d + grow, color);
        }
    }
}
