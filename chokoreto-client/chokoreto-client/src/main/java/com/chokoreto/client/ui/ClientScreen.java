package com.chokoreto.client.ui;

import com.chokoreto.client.Config;
import com.chokoreto.client.ui.pages.AuctionPage;
import com.chokoreto.client.ui.pages.ComingSoonPage;
import com.chokoreto.client.ui.pages.CrosshairPage;
import com.chokoreto.client.ui.pages.LockerPage;
import com.chokoreto.client.ui.pages.MusicPage;
import com.chokoreto.client.ui.pages.RecentPage;
import com.chokoreto.client.ui.pages.ScoreboardPage;
import com.chokoreto.client.ui.pages.SettingsPage;
import com.chokoreto.client.ui.pages.ThemePage;
import com.chokoreto.client.ui.pages.TransactionsPage;
import com.chokoreto.client.ui.pages.UtilitiesPage;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** The main menu: a sidebar with every module on the left and the selected page on the right. */
public class ClientScreen extends Screen {
    public static final int PW = 460;
    public static final int PH = 256;
    public static final int SIDE = 112;

    public enum Tab {
        AUCTION("Auction"), RECENT("Recent Auctions"), TRANSACTIONS("Transactions"), THEME("Theme"),
        CAPE("Cape"), LOCKER("Mini Locker"), MUSIC("Music"), CROSSHAIR("Crosshair"),
        UTILITIES("Utilities"), SCOREBOARD("Scoreboard"), SKY("Sky"), SETTINGS("Settings");

        public final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private static Tab lastTab = Tab.AUCTION;

    private final Map<Tab, Page> pages = new EnumMap<>(Tab.class);
    private Tab current;
    private int px;
    private int py;
    private W dragging;
    private final List<int[]> fields = new ArrayList<>();
    private final List<EditBox> boxes = new ArrayList<>();

    public ClientScreen() {
        this(lastTab);
    }

    public ClientScreen(Tab start) {
        super(Component.literal("Chokoreto"));
        this.current = start;
        pages.put(Tab.AUCTION, new AuctionPage());
        pages.put(Tab.RECENT, new RecentPage());
        pages.put(Tab.TRANSACTIONS, new TransactionsPage());
        pages.put(Tab.THEME, new ThemePage());
        pages.put(Tab.CAPE, new ComingSoonPage("Cape",
                "Custom capes change how your player is drawn.",
                "That needs a deeper (mixin) change that can't be safely tested yet,",
                "so this tab is reserved for a later update."));
        pages.put(Tab.LOCKER, new LockerPage());
        pages.put(Tab.MUSIC, new MusicPage());
        pages.put(Tab.CROSSHAIR, new CrosshairPage());
        pages.put(Tab.UTILITIES, new UtilitiesPage());
        pages.put(Tab.SCOREBOARD, new ScoreboardPage());
        pages.put(Tab.SKY, new ComingSoonPage("Sky",
                "A custom sky (colour / time of day) changes how the world is drawn.",
                "That needs a deeper (mixin) change that can't be safely tested yet,",
                "so this tab is reserved for a later update."));
        pages.put(Tab.SETTINGS, new SettingsPage());
        for (Page p : pages.values()) p.attach(this);
    }

    public Page page(Tab tab) {
        return pages.get(tab);
    }

    public AuctionPage auctionPage() {
        return (AuctionPage) pages.get(Tab.AUCTION);
    }

    public void open(Tab tab) {
        current = tab;
        lastTab = tab;
        rebuildWidgets();
    }

    public void rebuild() {
        rebuildWidgets();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ============================================================ building

    @Override
    protected void init() {
        px = (width - PW) / 2;
        py = (height - PH) / 2;
        fields.clear();
        boxes.clear();
        dragging = null;

        Page p = pages.get(current);
        p.ws.clear();
        p.setArea(px + SIDE + 10, py + 34, PW - SIDE - 20, PH - 42);
        p.build();
    }

    /** Creates a text box with a background drawn behind it. Used by the pages. */
    public EditBox edit(int x, int y, int w, int h, String hint, String value, int maxLength, Consumer<String> onChange) {
        EditBox box = new EditBox(this.font, x + 5, y, w - 10, h, Component.literal(hint));
        box.setBordered(false);
        box.setHint(Component.literal(hint));
        box.setMaxLength(maxLength);
        box.setValue(value);
        box.setResponder(onChange);
        addRenderableWidget(box);
        fields.add(new int[]{x, y, w, h});
        boxes.add(box);
        return box;
    }

    // ============================================================ input

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_RSHIFT && !typing()) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private boolean typing() {
        for (EditBox b : boxes) if (b.isFocused()) return true;
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();

        if (event.button() == 0) {
            // sidebar
            Tab[] tabs = Tab.values();
            for (int i = 0; i < tabs.length; i++) {
                if (Ui.in(mx, my, px + 6, py + 44 + i * 15, SIDE - 12, 15)) {
                    if (tabs[i] != current) open(tabs[i]);
                    return true;
                }
            }

            // widgets
            Page p = pages.get(current);
            for (int i = p.ws.size() - 1; i >= 0; i--) {
                W widget = p.ws.get(i);
                if (widget.click(mx, my)) {
                    dragging = widget;
                    Config.save();
                    return true;
                }
            }
            if (p.click(mx, my)) return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            dragging.drag(event.x(), event.y());
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (pages.get(current).scroll(scrollY)) return true;
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void onClose() {
        Config.save();
        super.onClose();
    }

    // ============================================================ drawing

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // we draw our own dim background
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mx, int my, float partialTick) {
        Config cfg = Config.get();
        g.fill(0, 0, width, height, 0x99000000);

        // panel
        Ui.round(g, px, py, PW, PH, Ui.pct(Ui.BG, cfg.panelOpacity));

        drawSidebar(g, mx, my);
        drawHeader(g);

        Page p = pages.get(current);
        p.draw(g, mx, my);

        // text box backgrounds (the real boxes are drawn by super, on top)
        for (int[] f : fields) {
            boolean focused = false;
            for (EditBox b : boxes) {
                if (b.getX() == f[0] + 5 && b.getY() == f[1] && b.isFocused()) focused = true;
            }
            Ui.field(g, f[0], f[1], f[2], f[3], focused);
        }

        for (W widget : p.ws) widget.draw(g, mx, my);

        super.extractRenderState(g, mx, my, partialTick);
    }

    private void drawSidebar(GuiGraphicsExtractor g, int mx, int my) {
        int sx = px + 4;
        int sy = py + 4;
        Ui.round(g, sx, sy, SIDE - 4, PH - 8, Ui.SURFACE | 0xFF000000);

        // brand
        Ui.round(g, px + 12, py + 12, 20, 20, Ui.accent());
        Ui.textCenter(g, "C", px + 22, py + 18, 0xFF15110C);
        Ui.text(g, "CHOKORETO", px + 38, py + 18, Ui.TEXT);

        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int ty = py + 44 + i * 15;
            boolean active = tabs[i] == current;
            boolean hover = Ui.in(mx, my, px + 6, ty, SIDE - 12, 15);
            if (active) {
                Ui.round(g, px + 8, ty, SIDE - 16, 14, Ui.alpha(Ui.accent(), 40));
                g.fill(px + 8, ty + 2, px + 10, ty + 12, Ui.accent());
            } else if (hover) {
                Ui.round(g, px + 8, ty, SIDE - 16, 14, Ui.SURFACE2 | 0xFF000000);
            }
            boolean soon = tabs[i] == Tab.CAPE || tabs[i] == Tab.SKY;
            int color = active ? Ui.accent() : soon ? 0xFF5C5967 : Ui.TEXT;
            Ui.text(g, tabs[i].label, px + 16, ty + 3, color);
            if (soon) Ui.textRight(g, "soon", px + SIDE - 12, ty + 3, 0xFF5C5967);
        }

        // user chip
        String name = minecraft != null ? minecraft.getUser().getName() : "Player";
        int cy = py + PH - 30;
        Ui.round(g, px + 8, cy, SIDE - 16, 22, Ui.SURFACE2 | 0xFF000000);
        Ui.round(g, px + 12, cy + 4, 14, 14, Ui.alpha(Ui.accent(), 80));
        Ui.text(g, Ui.fit(name, SIDE - 50), px + 31, cy + 7, Ui.TEXT);
        g.fill(px + SIDE - 18, cy + 9, px + SIDE - 14, cy + 13, Ui.GREEN);
    }

    private void drawHeader(GuiGraphicsExtractor g) {
        String name = minecraft != null ? minecraft.getUser().getName() : "Player";
        int hx = px + SIDE + 6;
        Ui.text(g, "Hello, " + name, hx + 4, py + 10, Ui.TEXT);
        String hint = "Right Shift to close";
        Ui.textRight(g, hint, px + PW - 10, py + 10, Ui.MUTED);
        g.fill(hx, py + 24, px + PW - 6, py + 25, Ui.alpha(Ui.accent(), 150));
    }

    /** Helper for pages: draw an item icon. */
    public static void drawItem(GuiGraphicsExtractor g, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) g.item(stack, x, y);
    }
}
