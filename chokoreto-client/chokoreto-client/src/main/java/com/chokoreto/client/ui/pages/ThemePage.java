package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Hud;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/** Pick the accent colour and how see-through the menu and HUD boxes are. */
public class ThemePage extends Page {
    private static final int[] COLORS = {
            0xE0A96D, // caramel
            0xFF8A65, // coral
            0xFF6B8B, // rose
            0xC792EA, // lavender
            0x7C9CFF, // periwinkle
            0x5CC8FF, // sky
            0x4DD9C0, // mint
            0x7BDB6B, // lime
            0xF5D547, // gold
            0xEDEBF0  // white
    };
    private static final String[] NAMES = {"Caramel", "Coral", "Rose", "Lavender", "Periwinkle", "Sky", "Mint", "Lime", "Gold", "White"};

    private String hex = "";

    @Override
    public void build() {
        ws.clear();
        Config c = Config.get();

        for (int i = 0; i < COLORS.length; i++) {
            int color = COLORS[i];
            ws.add(new W.Swatch(x + 8 + i * 24, y + 38, 18, color,
                    () -> (c.accent & 0xFFFFFF) == color,
                    () -> {
                        c.accent = color | 0xFF000000;
                        hex = "";
                        s.rebuild();
                    }));
        }

        s.edit(x + 8, y + 70, 84, 16, "Custom hex, e.g. FF8800", hex, 7, t -> hex = t);
        ws.add(new W.Btn(x + 98, y + 70, 50, 16, "Apply", () -> {
            String t = hex.trim().replace("#", "");
            if (t.length() == 6) {
                try {
                    c.accent = (int) (Long.parseLong(t, 16) | 0xFF000000L);
                    s.rebuild();
                } catch (NumberFormatException ignored) {
                }
            }
        }));

        ws.add(new W.Slider(x + 8, y + 108, 150, 24, "Menu opacity", 40, 100, "%",
                () -> c.panelOpacity, v -> c.panelOpacity = v));
        ws.add(new W.Slider(x + 8, y + 140, 150, 24, "HUD box opacity", 0, 100, "%",
                () -> c.hudOpacity, v -> c.hudOpacity = v));
        ws.add(new W.Toggle(x + 8, y + 172, 150, 16, "HUD text shadow", () -> c.textShadow, v -> c.textShadow = v));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, "Theme", "Pick an accent colour. Everything updates live.");
        Config c = Config.get();

        card(g, x, y + 26, 166, h - 28);
        Ui.text(g, "Accent", x + 8, y + 28, Ui.MUTED);

        // find the colour name
        String name = "Custom";
        for (int i = 0; i < COLORS.length; i++) if ((c.accent & 0xFFFFFF) == COLORS[i]) name = NAMES[i];
        Ui.textRight(g, name, x + 160, y + 28, Ui.TEXT);

        // preview card
        int px = x + 174;
        int pw = w - 174;
        card(g, px, y + 26, pw, h - 28);
        Ui.text(g, "PREVIEW", px + 8, y + 32, Ui.MUTED);

        int hy = y + 50;
        Hud.Content sample = new Hud.Content(null, List.of(
                new Hud.Line("Session profit", Ui.TEXT),
                new Hud.Line("+$12.5m", Ui.GREEN),
                new Hud.Line("Accent colour", Ui.accent())), true);
        Hud.draw(g, Ui.font(), sample, px + 8, hy);

        // fake button + toggle
        Ui.round(g, px + 8, hy + 56, 60, 16, Ui.SURFACE2 | 0xFF000000);
        Ui.roundOutline(g, px + 8, hy + 56, 60, 16, Ui.accent());
        Ui.textCenter(g, "Button", px + 38, hy + 60, Ui.accent());
        Ui.round(g, px + 76, hy + 60, 22, 10, Ui.accent());
        Ui.round(g, px + 89, hy + 61, 8, 8, 0xFFFFFFFF);
    }
}
