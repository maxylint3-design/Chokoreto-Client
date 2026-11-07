package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Crosshair;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Custom crosshair: style, colour, size, thickness, gap and outline, with a live preview. */
public class CrosshairPage extends Page {
    private static final int[] COLORS = {
            0xFFFFFF, 0xFF5555, 0x55FF55, 0x5CC8FF, 0xF5D547, 0xFF6BD6, 0xFF9A3C, 0x000000
    };

    @Override
    public void build() {
        ws.clear();
        Config c = Config.get();

        // style chips (3 per row)
        for (int i = 0; i < Crosshair.STYLES.length; i++) {
            String style = Crosshair.STYLES[i];
            ws.add(new W.Btn(x + (i % 3) * 72, y + 30 + (i / 3) * 20, 68, 16, Crosshair.STYLE_NAMES[i],
                    () -> c.crosshairStyle = style, () -> c.crosshairStyle.equals(style), null));
        }

        // colours
        for (int i = 0; i < COLORS.length; i++) {
            int color = COLORS[i];
            ws.add(new W.Swatch(x + 4 + i * 24, y + 100, 16, color,
                    () -> (c.crosshairColor & 0xFFFFFF) == color,
                    () -> c.crosshairColor = color | 0xFF000000));
        }

        ws.add(new W.Slider(x, y + 126, 100, 24, "Size", 1, 20, "", () -> c.crosshairSize, v -> c.crosshairSize = v));
        ws.add(new W.Slider(x + 108, y + 126, 100, 24, "Thickness", 1, 5, "", () -> c.crosshairThickness, v -> c.crosshairThickness = v));
        ws.add(new W.Slider(x, y + 158, 100, 24, "Gap", 0, 14, "", () -> c.crosshairGap, v -> c.crosshairGap = v));
        ws.add(new W.Toggle(x + 108, y + 160, 100, 16, "Outline", () -> c.crosshairOutline, v -> c.crosshairOutline = v));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, "Crosshair", "Pick a style - Vanilla keeps the normal one.");
        Config c = Config.get();

        Ui.text(g, "Colour", x, y + 88, Ui.MUTED);

        // live preview
        int px = x + 220;
        int pw = w - 220;
        card(g, px, y + 30, pw, 120);
        Ui.text(g, "PREVIEW", px + 8, y + 36, Ui.MUTED);
        int cx = px + pw / 2;
        int cy = y + 96;
        // a dark "world" square behind it so every colour is visible
        Ui.round(g, px + 10, y + 50, pw - 20, 90, 0xFF2B3A2F);
        if (Crosshair.isCustom()) {
            Crosshair.draw(g, cx, cy);
        } else {
            Ui.textCenter(g, "Vanilla", cx, cy - 4, Ui.MUTED);
        }
        if (c.crosshairStyle == null) c.crosshairStyle = "VANILLA";
    }
}
