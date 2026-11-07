package com.chokoreto.client.ui;

import com.chokoreto.client.Config;
import com.chokoreto.client.Hud;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/** Drag the HUD boxes wherever you like. Positions are saved when you let go. */
public class HudEditorScreen extends Screen {
    private final Screen parent;
    private Hud.Element dragging;
    private double grabX;
    private double grabY;

    /** x, y, w, h of every element on the last frame, in the same order as the list below. */
    private final List<int[]> rects = new ArrayList<>();
    private final List<Hud.Element> shown = new ArrayList<>();

    public HudEditorScreen(Screen parent) {
        super(Component.literal("HUD editor"));
        this.parent = parent;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        g.fill(0, 0, width, height, 0x66000000);

        Font font = this.font;
        rects.clear();
        shown.clear();

        for (Hud.Element e : Hud.ELEMENTS) {
            if (!e.enabled()) continue;
            Hud.Content c = e.content(true);
            if (c == null) continue;
            int[] size = Hud.measure(font, c);
            int[] at = Hud.place(e.pos(), size[0], size[1], width, height);
            Hud.draw(g, font, c, at[0], at[1]);

            boolean active = e == dragging || Ui.in(mx, my, at[0], at[1], size[0], size[1]);
            Ui.roundOutline(g, at[0] - 2, at[1] - 2, size[0] + 4, size[1] + 4,
                    active ? Ui.accent() : 0x66FFFFFF);
            g.text(font, e.name(), at[0], at[1] - 12, active ? Ui.accent() : 0xAAFFFFFF, true);

            rects.add(new int[]{at[0], at[1], size[0], size[1]});
            shown.add(e);
        }

        // instructions + buttons
        String help = "Drag a box to move it";
        g.text(font, help, (width - font.width(help)) / 2, height / 2 - 20, 0xFFFFFFFF, true);
        String help2 = "Only boxes that are switched on are shown";
        g.text(font, help2, (width - font.width(help2)) / 2, height / 2 - 8, 0xFF9A98A6, true);

        drawButton(g, mx, my, doneX(), doneY(), "Done");
        drawButton(g, mx, my, resetX(), doneY(), "Reset positions");
    }

    private int doneX() {
        return width / 2 - 104;
    }

    private int resetX() {
        return width / 2 + 4;
    }

    private int doneY() {
        return height / 2 + 6;
    }

    private void drawButton(GuiGraphicsExtractor g, int mx, int my, int bx, int by, String label) {
        boolean hover = Ui.in(mx, my, bx, by, 100, 18);
        Ui.round(g, bx, by, 100, 18, (hover ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
        Ui.roundOutline(g, bx, by, 100, 18, Ui.accent());
        g.text(font, label, bx + (100 - font.width(label)) / 2, by + 5, 0xFFFFFFFF, false);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (event.button() == 0) {
            if (Ui.in(mx, my, doneX(), doneY(), 100, 18)) {
                onClose();
                return true;
            }
            if (Ui.in(mx, my, resetX(), doneY(), 100, 18)) {
                Config.get().resetHudPositions();
                Config.save();
                return true;
            }
            for (int i = shown.size() - 1; i >= 0; i--) {
                int[] r = rects.get(i);
                if (Ui.in(mx, my, r[0], r[1], r[2], r[3])) {
                    dragging = shown.get(i);
                    grabX = mx - r[0];
                    grabY = my - r[1];
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            Config.Pos pos = dragging.pos();
            pos.x = Math.max(0, (int) Math.round(event.x() - grabX));
            pos.y = Math.max(0, (int) Math.round(event.y() - grabY));
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            Config.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Config.save();
        this.minecraft.gui.setScreen(parent);
    }
}
