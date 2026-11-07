package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Music;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Play Minecraft's music discs while you work. */
public class MusicPage extends Page {
    @Override
    public void build() {
        ws.clear();

        ws.add(new W.Slider(x, y + 30, 150, 24, "Volume", 0, 100, "%",
                () -> Config.get().musicVolume, v -> Config.get().musicVolume = v));
        ws.add(new W.Btn(x + w - 70, y + 32, 70, 16, "Stop", Music::stop, null, () -> Music.nowPlaying() != null));

        int cols = 3;
        int bw = (w - (cols - 1) * 4) / cols;
        for (int i = 0; i < Music.NAMES.length; i++) {
            int index = i;
            int bx = x + (i % cols) * (bw + 4);
            int by = y + 62 + (i / cols) * 20;
            String name = Music.NAMES[i];
            ws.add(new W.Btn(bx, by, bw, 16, name, () -> Music.play(index),
                    () -> name.equals(Music.nowPlaying()), null));
        }
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, "Music", "Plays Minecraft's own music discs. Click a track.");
        String now = Music.nowPlaying();
        Ui.textRight(g, now == null ? "Nothing playing" : "Now playing: " + now, x + w, y, now == null ? Ui.MUTED : Ui.accent());
    }
}
