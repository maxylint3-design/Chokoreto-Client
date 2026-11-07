package com.chokoreto.client.ui;

import com.chokoreto.client.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Colours and small drawing helpers shared by the whole UI. Flat, dark and minimal. */
public final class Ui {
    private Ui() {}

    // palette (RGB, alpha added where used)
    public static final int BG = 0x0E0D12;
    public static final int SURFACE = 0x17151C;
    public static final int SURFACE2 = 0x211E28;
    public static final int SURFACE3 = 0x2C2835;
    public static final int TEXT = 0xFFEDEBF0;
    public static final int MUTED = 0xFF8D8A98;
    public static final int GREEN = 0xFF5DD28A;
    public static final int RED = 0xFFFF6B6B;

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static int accent() {
        return Config.get().accent | 0xFF000000;
    }

    /** Same colour but with a different alpha (0-255). */
    public static int alpha(int rgb, int a) {
        return ((a & 0xFF) << 24) | (rgb & 0xFFFFFF);
    }

    /** Same colour but with a percentage opacity (0-100). */
    public static int pct(int rgb, int percent) {
        return alpha(rgb, Math.max(0, Math.min(100, percent)) * 255 / 100);
    }

    public static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    /** Filled rectangle with the 4 corner pixels cut off - looks rounded at small sizes. */
    public static void round(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    /** Matching 1px outline for {@link #round}. */
    public static void roundOutline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x + 1, y, x + w - 1, y + 1, color);
        g.fill(x + 1, y + h - 1, x + w - 1, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    public static void text(GuiGraphicsExtractor g, String s, int x, int y, int color) {
        g.text(font(), s, x, y, color, false);
    }

    public static void textRight(GuiGraphicsExtractor g, String s, int right, int y, int color) {
        g.text(font(), s, right - font().width(s), y, color, false);
    }

    public static void textCenter(GuiGraphicsExtractor g, String s, int centerX, int y, int color) {
        g.text(font(), s, centerX - font().width(s) / 2, y, color, false);
    }

    /** Cuts text so it fits in the given pixel width. */
    public static String fit(String s, int width) {
        return font().plainSubstrByWidth(s, width);
    }

    /** Background box for a text field (the real EditBox is drawn on top of it). */
    public static void field(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean focused) {
        round(g, x, y, w, h, SURFACE2 | 0xFF000000);
        if (focused) roundOutline(g, x, y, w, h, accent());
    }
}
