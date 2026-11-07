package com.chokoreto.client.ui.pages;

import com.chokoreto.client.AuctionManager;
import com.chokoreto.client.Config;
import com.chokoreto.client.Format;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The 3-step auction wizard: 1 Item -> 2 Price -> 3 Time -> Start. */
public class AuctionPage extends Page {
    private static final int COLS = 9;
    private static final int ROWS = 5;
    private static final int CELL = 22;
    private static final int LEFT_W = 208;

    private static final String[] PRICE_LABELS = {"$100k", "$500k", "$1m", "$5m", "$10m", "$50m"};
    private static final long[] PRICE_VALUES = {100_000L, 500_000L, 1_000_000L, 5_000_000L, 10_000_000L, 50_000_000L};
    private static final String[] TIME_LABELS = {"1 min", "5 min", "10 min", "30 min", "1 hour", "3 hours", "6 hours", "12 hours", "24 hours"};
    private static final int[] TIME_SECONDS = {60, 300, 600, 1800, 3600, 10800, 21600, 43200, 86400};

    private static List<ItemStack> allItems;

    private static List<ItemStack> allItems() {
        if (allItems == null) {
            allItems = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                if (item != Items.AIR) allItems.add(new ItemStack(item));
            }
        }
        return allItems;
    }

    private enum Filter { ALL, BLOCKS, ITEMS }

    private int step = 1;
    private ItemStack selected = ItemStack.EMPTY;
    private int count = 1;
    private String priceText = "1m";
    private long minBid = 1_000_000L;
    private int durationSeconds = 60;
    private String customMinutes = "";
    private boolean announce = Config.get().announceDefault;
    private Filter filter = Filter.ALL;
    private String search = "";
    private int scrollRow = 0;
    private String status = "";
    private final List<ItemStack> visible = new ArrayList<>();
    private ItemStack hovered = ItemStack.EMPTY;

    public AuctionPage() {
        refilter();
    }

    /** Used by the Mini Locker and Recent Auctions tabs ("Auction again"). */
    public void prefill(ItemStack stack, int count, long minBid) {
        this.selected = stack.copyWithCount(1);
        this.count = Math.max(1, count);
        this.minBid = minBid;
        this.priceText = Format.shortNumber(minBid);
        this.step = 2;
    }

    // ================================================================ build

    @Override
    public void build() {
        ws.clear();

        String[] names = {"1  Item", "2  Price", "3  Time"};
        for (int i = 0; i < 3; i++) {
            int n = i + 1;
            ws.add(new W.Btn(x + i * 70, y, 66, 14, names[i], () -> goStep(n),
                    () -> step == n, () -> n == 1 || !selected.isEmpty()));
        }

        if (step == 1) buildItems();
        if (step == 2) buildPrice();
        if (step == 3) buildTime();

        // back / next
        ws.add(new W.Btn(x, y + h - 18, 60, 16, "Back", () -> goStep(step - 1), null, () -> step > 1));
        if (step < 3) {
            ws.add(new W.Btn(x + LEFT_W - 60, y + h - 18, 60, 16, "Next", () -> goStep(step + 1),
                    null, this::canAdvance));
        }

        // start button under the preview
        ws.add(new W.Btn(x + LEFT_W + 8, y + h - 18, w - LEFT_W - 8, 16, "Start Auction", this::start,
                null, () -> problem() == null));
    }

    private void goStep(int n) {
        if (n < 1 || n > 3) return;
        if (n > 1 && selected.isEmpty()) return;
        step = n;
        s.rebuild();
    }

    private void buildItems() {
        s.edit(x, y + 22, 104, 16, "Search every item...", search, 40, t -> {
            search = t;
            scrollRow = 0;
            refilter();
        });

        String[] names = {"All", "Blocks", "Items"};
        int[] widths = {30, 42, 36};
        Filter[] filters = Filter.values();
        int fx = x + 108;
        for (int i = 0; i < 3; i++) {
            Filter f = filters[i];
            ws.add(new W.Btn(fx, y + 22, widths[i], 16, names[i], () -> {
                filter = f;
                scrollRow = 0;
                refilter();
            }, () -> filter == f, null));
            fx += widths[i] + 2;
        }

        ws.add(new W.Btn(x + LEFT_W - 100, y + 160, 100, 16, "Use held item", this::useHeld));
    }

    private void buildPrice() {
        s.edit(x, y + 34, 100, 16, "e.g. 1m or 500k", priceText, 20, t -> {
            priceText = t;
            minBid = Format.parseMoney(t);
        });
        s.edit(x + 108, y + 34, 48, 16, "Count", Integer.toString(count), 4, t -> {
            try {
                count = Integer.parseInt(t.trim());
            } catch (NumberFormatException e) {
                count = 0;
            }
        });
        for (int i = 0; i < PRICE_LABELS.length; i++) {
            long value = PRICE_VALUES[i];
            ws.add(new W.Btn(x + i * 35, y + 66, 33, 16, PRICE_LABELS[i], () -> {
                priceText = Format.shortNumber(value);
                minBid = value;
                s.rebuild();
            }, () -> minBid == value, null));
        }
    }

    private void buildTime() {
        for (int i = 0; i < TIME_LABELS.length; i++) {
            int seconds = TIME_SECONDS[i];
            ws.add(new W.Btn(x + (i % 3) * 70, y + 28 + (i / 3) * 20, 66, 16, TIME_LABELS[i], () -> {
                durationSeconds = seconds;
                customMinutes = "";
                s.rebuild();
            }, () -> customMinutes.isEmpty() && durationSeconds == seconds, null));
        }
        s.edit(x, y + 92, 136, 16, "Custom (minutes)", customMinutes, 8, t -> {
            customMinutes = t;
            if (!t.isBlank()) {
                try {
                    durationSeconds = (int) Math.round(Double.parseDouble(t.trim()) * 60.0);
                } catch (NumberFormatException e) {
                    durationSeconds = -1;
                }
            }
        });
        ws.add(new W.Toggle(x, y + 116, LEFT_W - 2, 16, "Announce in chat", () -> announce, v -> announce = v));
    }

    // ================================================================ logic

    private void refilter() {
        visible.clear();
        String needle = search.trim().toLowerCase(Locale.ROOT);
        for (ItemStack stack : allItems()) {
            boolean isBlock = stack.getItem() instanceof BlockItem;
            if (filter == Filter.BLOCKS && !isBlock) continue;
            if (filter == Filter.ITEMS && isBlock) continue;
            if (!needle.isEmpty()) {
                String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
                String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
                if (!name.contains(needle) && !id.contains(needle)) continue;
            }
            visible.add(stack);
        }
        scrollRow = Math.min(scrollRow, maxScroll());
    }

    private int maxScroll() {
        int rows = (visible.size() + COLS - 1) / COLS;
        return Math.max(0, rows - ROWS);
    }

    private void useHeld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) {
            status = "Your hand is empty!";
            return;
        }
        selected = held.copy();
        count = held.getCount();
        status = "";
    }

    private boolean canAdvance() {
        if (step == 1) return !selected.isEmpty();
        if (step == 2) return minBid > 0 && count >= 1;
        return false;
    }

    private String problem() {
        if (selected.isEmpty()) return "Pick an item first";
        if (minBid <= 0) return "Enter a valid minimum bid";
        if (count < 1) return "Count must be 1 or more";
        if (durationSeconds < 10) return "Time must be at least 10 seconds";
        if (durationSeconds > 7 * 86400) return "Max time is 7 days";
        return null;
    }

    private void start() {
        if (problem() != null) return;
        AuctionManager.start(selected, count, minBid, durationSeconds, announce);
        s.onClose();
    }

    // ================================================================ input

    @Override
    public boolean click(double mx, double my) {
        if (step != 1) return false;
        int col = (int) Math.floor((mx - x) / CELL);
        int row = (int) Math.floor((my - (y + 44)) / CELL);
        if (col >= 0 && row >= 0 && col < COLS && row < ROWS) {
            int index = (scrollRow + row) * COLS + col;
            if (index < visible.size()) {
                selected = visible.get(index).copy();
                count = 1;
                status = "";
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean scroll(double amount) {
        if (step != 1) return false;
        scrollRow -= (int) Math.signum(amount);
        scrollRow = Math.max(0, Math.min(maxScroll(), scrollRow));
        return true;
    }

    // ================================================================ draw

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        hovered = ItemStack.EMPTY;
        if (step == 1) drawGrid(g, mx, my);
        if (step == 2) drawPriceInfo(g);
        if (step == 3) drawTimeInfo(g);
        drawPreview(g);
        if (!hovered.isEmpty()) {
            g.setTooltipForNextFrame(Ui.font(), hovered, mx, my);
        }
    }

    private void drawGrid(GuiGraphicsExtractor g, int mx, int my) {
        int gx = x;
        int gy = y + 44;
        Ui.round(g, gx - 2, gy - 2, COLS * CELL + 4, ROWS * CELL + 4, Ui.SURFACE | 0xFF000000);

        if (visible.isEmpty()) {
            Ui.textCenter(g, "No items match", gx + COLS * CELL / 2, gy + 48, Ui.MUTED);
        }

        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                int index = (scrollRow + r) * COLS + c;
                if (index >= visible.size()) break;
                ItemStack stack = visible.get(index);
                int ix = gx + c * CELL;
                int iy = gy + r * CELL;
                boolean isSelected = !selected.isEmpty() && selected.getItem() == stack.getItem();
                boolean isHover = Ui.in(mx, my, ix, iy, CELL, CELL);

                if (isSelected) {
                    Ui.round(g, ix, iy, CELL, CELL, Ui.alpha(Ui.accent(), 60));
                    Ui.roundOutline(g, ix, iy, CELL, CELL, Ui.accent());
                } else if (isHover) {
                    Ui.round(g, ix, iy, CELL, CELL, 0x30FFFFFF);
                }
                if (isHover) hovered = stack;
                g.item(stack, ix + 3, iy + 3);
            }
        }

        int max = maxScroll();
        if (max > 0) {
            int trackH = ROWS * CELL;
            int totalRows = (visible.size() + COLS - 1) / COLS;
            int thumbH = Math.max(10, trackH * ROWS / totalRows);
            int thumbY = gy + (trackH - thumbH) * scrollRow / max;
            int sx = gx + COLS * CELL + 4;
            g.fill(sx, gy, sx + 3, gy + trackH, 0x30FFFFFF);
            g.fill(sx, thumbY, sx + 3, thumbY + thumbH, Ui.accent());
        }

        String hint = status.isEmpty() ? "Click an item, or hold one." : status;
        Ui.text(g, hint, x, y + 164, status.isEmpty() ? Ui.MUTED : Ui.RED);
    }

    private void drawPriceInfo(GuiGraphicsExtractor g) {
        Ui.text(g, "Minimum bid", x, y + 22, Ui.TEXT);
        Ui.text(g, "Count", x + 108, y + 22, Ui.TEXT);
        Ui.text(g, "Quick picks", x, y + 56, Ui.MUTED);
        if (minBid > 0) {
            Ui.text(g, "= " + Format.moneyFull(minBid), x, y + 94, Ui.GREEN);
        } else {
            Ui.text(g, "Type an amount like 1m, 500k or 2.5b", x, y + 94, Ui.RED);
        }
        Ui.text(g, "Payments below this are ignored.", x, y + 108, Ui.MUTED);
    }

    private void drawTimeInfo(GuiGraphicsExtractor g) {
        Ui.text(g, "How long should it last?", x, y + 17, Ui.TEXT);
        Ui.text(g, "Posts the auction, bids and winner in public chat.", x, y + 138, Ui.MUTED);
    }

    private void drawPreview(GuiGraphicsExtractor g) {
        int px = x + LEFT_W + 8;
        int pw = w - LEFT_W - 8;
        card(g, px, y, pw, h - 24);

        Ui.text(g, "PREVIEW", px + 8, y + 8, Ui.MUTED);

        Ui.round(g, px + 8, y + 22, 36, 36, Ui.SURFACE2 | 0xFF000000);
        if (selected.isEmpty()) {
            Ui.textCenter(g, "?", px + 26, y + 36, Ui.MUTED);
        } else {
            Matrix3x2fStack pose = g.pose();
            pose.pushMatrix();
            pose.translate(px + 10, y + 24);
            pose.scale(2f, 2f);
            g.item(selected, 0, 0);
            pose.popMatrix();
        }

        String name = selected.isEmpty() ? "No item yet" : selected.getHoverName().getString();
        Ui.text(g, Ui.fit(name, pw - 56), px + 50, y + 28, selected.isEmpty() ? Ui.MUTED : Ui.TEXT);
        if (!selected.isEmpty()) Ui.text(g, "x" + count, px + 50, y + 40, Ui.MUTED);

        row(g, px, pw, y + 70, "Min bid", minBid > 0 ? Format.money(minBid) : "?");
        row(g, px, pw, y + 83, "Duration", durationSeconds > 0 ? Format.duration(durationSeconds) : "?");
        row(g, px, pw, y + 96, "Announce", announce ? "On" : "Off");

        String problem = problem();
        wrapped(g, problem != null ? problem : "Ready to go!", px + 8, y + 116, pw - 16,
                problem != null ? Ui.RED : Ui.GREEN);
    }

    private void row(GuiGraphicsExtractor g, int px, int pw, int ry, String label, String value) {
        Ui.text(g, label, px + 8, ry, Ui.MUTED);
        Ui.textRight(g, value, px + pw - 8, ry, Ui.TEXT);
    }

    private void wrapped(GuiGraphicsExtractor g, String message, int tx, int ty, int maxWidth, int color) {
        StringBuilder line = new StringBuilder();
        for (String word : message.split(" ")) {
            String attempt = line.length() == 0 ? word : line + " " + word;
            if (Ui.font().width(attempt) > maxWidth && line.length() > 0) {
                Ui.text(g, line.toString(), tx, ty, color);
                ty += 10;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(attempt);
            }
        }
        if (line.length() > 0) Ui.text(g, line.toString(), tx, ty, color);
    }
}
