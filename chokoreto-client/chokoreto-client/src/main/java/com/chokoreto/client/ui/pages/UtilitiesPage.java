package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Format;
import com.chokoreto.client.Store;
import com.chokoreto.client.Utilities;
import com.chokoreto.client.ui.HudEditorScreen;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/** Info panel, nearby-player detection and timers. */
public class UtilitiesPage extends Page {
    private static int sub = 0; // 0 info, 1 players, 2 timers
    private String timerLabel = "";
    private String timerMinutes = "";

    @Override
    public void build() {
        ws.clear();
        Config c = Config.get();

        String[] names = {"Info panel", "Nearby players", "Timers"};
        int[] widths = {80, 96, 70};
        int tx = x;
        for (int i = 0; i < 3; i++) {
            int n = i;
            ws.add(new W.Btn(tx, y, widths[i], 14, names[i], () -> {
                sub = n;
                s.rebuild();
            }, () -> sub == n, null));
            tx += widths[i] + 4;
        }

        if (sub == 0) {
            ws.add(new W.Toggle(x + 8, y + 30, 150, 16, "Show info panel", () -> c.infoShow, v -> c.infoShow = v));
            ws.add(new W.Toggle(x + 8, y + 52, 150, 16, "FPS", () -> c.infoFps, v -> c.infoFps = v));
            ws.add(new W.Toggle(x + 8, y + 70, 150, 16, "Ping", () -> c.infoPing, v -> c.infoPing = v));
            ws.add(new W.Toggle(x + 8, y + 88, 150, 16, "Coordinates", () -> c.infoCoords, v -> c.infoCoords = v));
            ws.add(new W.Toggle(x + 8, y + 106, 150, 16, "Direction", () -> c.infoDirection, v -> c.infoDirection = v));
            ws.add(new W.Toggle(x + 8, y + 124, 150, 16, "Totem count", () -> c.infoTotems, v -> c.infoTotems = v));
            ws.add(new W.Toggle(x + 8, y + 142, 150, 16, "Crystal count", () -> c.infoCrystals, v -> c.infoCrystals = v));
            ws.add(new W.Toggle(x + 8, y + 160, 150, 16, "Golden apple count", () -> c.infoGapples, v -> c.infoGapples = v));
            ws.add(new W.Btn(x + 176, y + 30, 100, 16, "Edit position", this::editPositions));
        }

        if (sub == 1) {
            ws.add(new W.Toggle(x + 8, y + 30, 200, 16, "Show nearby players list", () -> c.nearbyShow, v -> c.nearbyShow = v));
            ws.add(new W.Toggle(x + 8, y + 52, 200, 16, "Chat alert when someone arrives", () -> c.nearbyAlert, v -> c.nearbyAlert = v));
            ws.add(new W.Slider(x + 8, y + 80, 200, 24, "Detection radius", 8, 128, " blocks",
                    () -> c.nearbyRadius, v -> c.nearbyRadius = v));
            ws.add(new W.Btn(x + 8, y + 116, 100, 16, "Edit position", this::editPositions));
        }

        if (sub == 2) {
            ws.add(new W.Toggle(x + 8, y + 30, 200, 16, "Show timers on screen", () -> c.timersShow, v -> c.timersShow = v));
            s.edit(x + 8, y + 62, 120, 16, "Label, e.g. Drill", timerLabel, 20, t -> timerLabel = t);
            s.edit(x + 134, y + 62, 60, 16, "Minutes", timerMinutes, 6, t -> timerMinutes = t);
            ws.add(new W.Btn(x + 200, y + 62, 50, 16, "Add", () -> {
                try {
                    double minutes = Double.parseDouble(timerMinutes.trim());
                    if (minutes > 0) {
                        Utilities.addTimer(timerLabel, Math.max(1, Math.round(minutes)));
                        timerLabel = "";
                        timerMinutes = "";
                        s.rebuild();
                    }
                } catch (NumberFormatException ignored) {
                }
            }));
            ws.add(new W.Btn(x + 256, y + 62, 70, 16, "Position", this::editPositions));

            List<Store.TimerEntry> timers = Store.get().timers;
            for (int i = 0; i < timers.size() && i < 6; i++) {
                Store.TimerEntry t = timers.get(i);
                ws.add(new W.Btn(x + w - 22, y + 92 + i * 18, 16, 14, "x", () -> {
                    Store.get().timers.remove(t);
                    Store.save();
                    s.rebuild();
                }));
            }
        }
    }

    private void editPositions() {
        Minecraft.getInstance().gui.setScreen(new HudEditorScreen(s));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        if (sub == 0) {
            card(g, x, y + 24, 164, 158);
            Ui.text(g, "Shown on your screen, top-left by default.", x + 176, y + 54, Ui.MUTED);
            Ui.text(g, "Drag it anywhere with Edit position.", x + 176, y + 66, Ui.MUTED);
        }
        if (sub == 1) {
            card(g, x, y + 24, 216, 116);
            Ui.text(g, "Shows other players inside the radius,", x + 8, y + 150, Ui.MUTED);
            Ui.text(g, "closest first, and tells you in chat.", x + 8, y + 162, Ui.MUTED);
        }
        if (sub == 2) {
            card(g, x, y + 24, w, 32);
            Ui.text(g, "New timer", x + 8, y + 46, Ui.MUTED);
            List<Store.TimerEntry> timers = Store.get().timers;
            if (timers.isEmpty()) {
                Ui.text(g, "No timers running. Add one above (e.g. Drill, 30 minutes).", x, y + 100, Ui.MUTED);
            }
            long now = System.currentTimeMillis();
            for (int i = 0; i < timers.size() && i < 6; i++) {
                Store.TimerEntry t = timers.get(i);
                int ry = y + 92 + i * 18;
                Ui.round(g, x, ry, w - 26, 14, Ui.SURFACE | 0xFF000000);
                Ui.text(g, Ui.fit(t.label, 200), x + 6, ry + 3, Ui.TEXT);
                Ui.textRight(g, Format.clock(t.endAt - now), x + w - 32, ry + 3, Ui.accent());
            }
        }
    }
}
