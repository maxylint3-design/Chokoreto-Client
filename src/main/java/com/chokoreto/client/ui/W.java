package com.chokoreto.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * A tiny widget: something you can draw and click. All the menu's buttons, toggles,
 * sliders and colour swatches are made from these.
 */
public abstract class W {
    public int x;
    public int y;
    public int w;
    public int h;

    protected W(int x, int y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

    public boolean contains(double mx, double my) {
        return Ui.in(mx, my, x, y, w, h);
    }

    public abstract void draw(GuiGraphicsExtractor g, int mx, int my);

    /** @return true if the click was used */
    public boolean click(double mx, double my) {
        return false;
    }

    public void drag(double mx, double my) {
    }

    // ---------------------------------------------------------------- button / chip

    public static final class Btn extends W {
        private final String label;
        private final Runnable action;
        private final BooleanSupplier selected;
        private final BooleanSupplier enabled;

        public Btn(int x, int y, int w, int h, String label, Runnable action) {
            this(x, y, w, h, label, action, null, null);
        }

        public Btn(int x, int y, int w, int h, String label, Runnable action,
                   BooleanSupplier selected, BooleanSupplier enabled) {
            super(x, y, w, h);
            this.label = label;
            this.action = action;
            this.selected = selected;
            this.enabled = enabled;
        }

        private boolean isEnabled() {
            return enabled == null || enabled.getAsBoolean();
        }

        @Override
        public void draw(GuiGraphicsExtractor g, int mx, int my) {
            boolean on = selected != null && selected.getAsBoolean();
            boolean en = isEnabled();
            boolean hover = en && contains(mx, my);
            int bg = hover ? Ui.SURFACE3 : Ui.SURFACE2;
            Ui.round(g, x, y, w, h, bg | 0xFF000000);
            if (on) Ui.roundOutline(g, x, y, w, h, Ui.accent());
            int color = !en ? 0xFF55525E : on ? Ui.accent() : Ui.TEXT;
            Ui.textCenter(g, Ui.fit(label, w - 4), x + w / 2, y + (h - 8) / 2, color);
        }

        @Override
        public boolean click(double mx, double my) {
            if (!contains(mx, my) || !isEnabled()) return false;
            action.run();
            return true;
        }
    }

    // ---------------------------------------------------------------- toggle switch

    public static final class Toggle extends W {
        private final String label;
        private final BooleanSupplier get;
        private final Consumer<Boolean> set;

        public Toggle(int x, int y, int w, int h, String label, BooleanSupplier get, Consumer<Boolean> set) {
            super(x, y, w, h);
            this.label = label;
            this.get = get;
            this.set = set;
        }

        @Override
        public void draw(GuiGraphicsExtractor g, int mx, int my) {
            boolean on = get.getAsBoolean();
            Ui.text(g, Ui.fit(label, w - 30), x, y + (h - 8) / 2, Ui.TEXT);
            int sx = x + w - 22;
            int sy = y + (h - 10) / 2;
            Ui.round(g, sx, sy, 22, 10, on ? Ui.accent() : (Ui.SURFACE3 | 0xFF000000));
            int knobX = on ? sx + 13 : sx + 1;
            Ui.round(g, knobX, sy + 1, 8, 8, 0xFFFFFFFF);
        }

        @Override
        public boolean click(double mx, double my) {
            if (!contains(mx, my)) return false;
            set.accept(!get.getAsBoolean());
            return true;
        }
    }

    // ---------------------------------------------------------------- slider

    public static final class Slider extends W {
        private final String label;
        private final int min;
        private final int max;
        private final String suffix;
        private final IntSupplier get;
        private final IntConsumer set;

        public Slider(int x, int y, int w, int h, String label, int min, int max, String suffix,
                      IntSupplier get, IntConsumer set) {
            super(x, y, w, h);
            this.label = label;
            this.min = min;
            this.max = max;
            this.suffix = suffix;
            this.get = get;
            this.set = set;
        }

        @Override
        public void draw(GuiGraphicsExtractor g, int mx, int my) {
            int value = get.getAsInt();
            Ui.text(g, label, x, y, Ui.TEXT);
            Ui.textRight(g, value + suffix, x + w, y, Ui.MUTED);
            int ty = y + 13;
            Ui.round(g, x, ty, w, 4, Ui.SURFACE3 | 0xFF000000);
            float t = (value - min) / (float) (max - min);
            int fillW = Math.max(2, (int) (w * t));
            Ui.round(g, x, ty, fillW, 4, Ui.accent());
            Ui.round(g, x + Math.min(w - 6, Math.max(0, fillW - 3)), ty - 2, 6, 8, 0xFFFFFFFF);
        }

        private void setFromMouse(double mx) {
            double t = Math.max(0, Math.min(1, (mx - x) / (double) w));
            set.accept((int) Math.round(min + t * (max - min)));
        }

        @Override
        public boolean click(double mx, double my) {
            if (!contains(mx, my)) return false;
            setFromMouse(mx);
            return true;
        }

        @Override
        public void drag(double mx, double my) {
            setFromMouse(mx);
        }
    }

    // ---------------------------------------------------------------- colour swatch

    public static final class Swatch extends W {
        private final int color;
        private final BooleanSupplier selected;
        private final Runnable action;

        public Swatch(int x, int y, int size, int color, BooleanSupplier selected, Runnable action) {
            super(x, y, size, size);
            this.color = color | 0xFF000000;
            this.selected = selected;
            this.action = action;
        }

        @Override
        public void draw(GuiGraphicsExtractor g, int mx, int my) {
            Ui.round(g, x, y, w, h, color);
            if (selected.getAsBoolean()) {
                Ui.roundOutline(g, x - 2, y - 2, w + 4, h + 4, 0xFFFFFFFF);
            } else if (contains(mx, my)) {
                Ui.roundOutline(g, x - 1, y - 1, w + 2, h + 2, 0x88FFFFFF);
            }
        }

        @Override
        public boolean click(double mx, double my) {
            if (!contains(mx, my)) return false;
            action.run();
            return true;
        }
    }
}
