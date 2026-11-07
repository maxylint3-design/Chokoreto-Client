package com.chokoreto.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * One tab of the menu. {@link #build} creates the widgets (buttons, text boxes),
 * {@link #draw} paints everything that is not a widget.
 */
public abstract class Page {
    protected ClientScreen s;
    public final List<W> ws = new ArrayList<>();

    // the area this page may use
    protected int x;
    protected int y;
    protected int w;
    protected int h;

    public final void attach(ClientScreen screen) {
        this.s = screen;
    }

    public final void setArea(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    /** Create widgets. Called every time the screen is (re)built. */
    public abstract void build();

    /** Draw backgrounds, text and lists. Widgets in {@link #ws} are drawn afterwards. */
    public abstract void draw(GuiGraphicsExtractor g, int mx, int my);

    /** Extra click handling for things that are not widgets (lists, grids). */
    public boolean click(double mx, double my) {
        return false;
    }

    public boolean scroll(double amount) {
        return false;
    }

    // ---------------------------------------------------------------- helpers

    protected void card(GuiGraphicsExtractor g, int cx, int cy, int cw, int ch) {
        Ui.round(g, cx, cy, cw, ch, Ui.SURFACE | 0xFF000000);
    }

    protected void title(GuiGraphicsExtractor g, String title, String subtitle) {
        Ui.text(g, title, x, y, Ui.TEXT);
        if (subtitle != null) Ui.text(g, Ui.fit(subtitle, w), x, y + 11, Ui.MUTED);
    }
}
