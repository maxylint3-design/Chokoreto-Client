package com.chokoreto.client.ui.pages;

import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Placeholder for features that are planned but not built yet. */
public class ComingSoonPage extends Page {
    private final String name;
    private final String[] lines;

    public ComingSoonPage(String name, String... lines) {
        this.name = name;
        this.lines = lines;
    }

    @Override
    public void build() {
        ws.clear();
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, name, "Coming soon");
        card(g, x, y + 26, w, 100);
        Ui.textCenter(g, name + " is planned for a later update", x + w / 2, y + 44, Ui.accent());
        int ly = y + 66;
        for (String line : lines) {
            Ui.textCenter(g, Ui.fit(line, w - 16), x + w / 2, ly, Ui.MUTED);
            ly += 12;
        }
    }
}
