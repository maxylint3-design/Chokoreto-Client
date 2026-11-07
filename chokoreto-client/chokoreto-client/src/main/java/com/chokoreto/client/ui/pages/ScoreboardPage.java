package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Scoreboard options. Right now: hide the sidebar scoreboard to clean up your screen. */
public class ScoreboardPage extends Page {
    @Override
    public void build() {
        ws.clear();
        Config c = Config.get();
        ws.add(new W.Toggle(x + 8, y + 34, 240, 16, "Hide the sidebar scoreboard", () -> c.hideScoreboard, v -> c.hideScoreboard = v));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        title(g, "Scoreboard", "Tidy up the right side of your screen.");
        card(g, x, y + 26, w, 34);
        Ui.text(g, "Tip: the Utilities tab can show info like ping and", x + 8, y + 70, Ui.MUTED);
        Ui.text(g, "item counts in a small box you place yourself.", x + 8, y + 82, Ui.MUTED);
    }
}
