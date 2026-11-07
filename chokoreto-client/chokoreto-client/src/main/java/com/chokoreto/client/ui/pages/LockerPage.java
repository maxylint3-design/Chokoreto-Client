package com.chokoreto.client.ui.pages;

import com.chokoreto.client.AuctionManager;
import com.chokoreto.client.Format;
import com.chokoreto.client.Stacks;
import com.chokoreto.client.Store;
import com.chokoreto.client.ui.ClientScreen;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Mini Locker: a shelf of items you auction often. Save what you are holding, set a usual
 * minimum bid, and start an auction in two clicks.
 */
public class LockerPage extends Page {
    private static final int ROW_H = 24;
    private static final long[] PRESETS = {100_000L, 500_000L, 1_000_000L, 5_000_000L, 10_000_000L, 50_000_000L, 100_000_000L};
    private int scroll = 0;

    @Override
    public void build() {
        ws.clear();
        ws.add(new W.Btn(x + w - 100, y, 100, 14, "Save held item", this::saveHeld));
    }

    private void saveHeld() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) {
            AuctionManager.info("Hold an item first, then press Save held item.");
            return;
        }
        String id = Stacks.idOf(held);
        for (Store.LockerItem existing : Store.get().locker) {
            if (existing.itemId.equals(id)) {
                existing.count = held.getCount();
                Store.save();
                return;
            }
        }
        Store.LockerItem item = new Store.LockerItem();
        item.itemId = id;
        item.count = held.getCount();
        Store.get().locker.add(item);
        Store.save();
    }

    private int visibleRows() {
        return (h - 30) / ROW_H;
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        List<Store.LockerItem> list = Store.get().locker;
        title(g, "Mini Locker", "Items you sell often. Click the price to change it.");

        if (list.isEmpty()) {
            card(g, x, y + 26, w, h - 30);
            Ui.textCenter(g, "Your locker is empty", x + w / 2, y + 80, Ui.TEXT);
            Ui.textCenter(g, "Hold an item and press Save held item.", x + w / 2, y + 94, Ui.MUTED);
            return;
        }

        int rows = visibleRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, list.size() - rows)));
        for (int i = 0; i < rows && scroll + i < list.size(); i++) {
            Store.LockerItem it = list.get(scroll + i);
            int ry = y + 28 + i * ROW_H;
            Ui.round(g, x, ry, w, ROW_H - 2, Ui.SURFACE | 0xFF000000);

            ItemStack stack = Stacks.stackOf(it.itemId);
            ClientScreen.drawItem(g, stack, x + 5, ry + 3);
            String name = stack.isEmpty() ? it.itemId : stack.getHoverName().getString();
            Ui.text(g, Ui.fit(it.count + "x " + name, 140), x + 28, ry + 8, Ui.TEXT);

            // price chip
            boolean ph = Ui.in(mx, my, x + w - 190, ry + 3, 56, 16);
            Ui.round(g, x + w - 190, ry + 3, 56, 16, (ph ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
            Ui.textCenter(g, Format.money(it.minBid), x + w - 162, ry + 7, Ui.accent());

            // auction + remove
            boolean ah = Ui.in(mx, my, x + w - 130, ry + 3, 70, 16);
            Ui.round(g, x + w - 130, ry + 3, 70, 16, (ah ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
            Ui.textCenter(g, "Auction", x + w - 95, ry + 7, Ui.TEXT);

            boolean rh = Ui.in(mx, my, x + w - 54, ry + 3, 50, 16);
            Ui.round(g, x + w - 54, ry + 3, 50, 16, (rh ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
            Ui.textCenter(g, "Remove", x + w - 29, ry + 7, Ui.RED);
        }
    }

    @Override
    public boolean click(double mx, double my) {
        List<Store.LockerItem> list = Store.get().locker;
        int rows = visibleRows();
        for (int i = 0; i < rows && scroll + i < list.size(); i++) {
            int ry = y + 28 + i * ROW_H;
            Store.LockerItem it = list.get(scroll + i);

            if (Ui.in(mx, my, x + w - 190, ry + 3, 56, 16)) {
                // cycle through usual prices
                int next = 0;
                for (int p = 0; p < PRESETS.length; p++) {
                    if (PRESETS[p] == it.minBid) next = (p + 1) % PRESETS.length;
                }
                it.minBid = PRESETS[next];
                Store.save();
                return true;
            }
            if (Ui.in(mx, my, x + w - 130, ry + 3, 70, 16)) {
                ItemStack stack = Stacks.stackOf(it.itemId);
                if (!stack.isEmpty()) {
                    s.auctionPage().prefill(stack, it.count, it.minBid);
                    s.open(ClientScreen.Tab.AUCTION);
                }
                return true;
            }
            if (Ui.in(mx, my, x + w - 54, ry + 3, 50, 16)) {
                list.remove(scroll + i);
                Store.save();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean scroll(double amount) {
        scroll -= (int) Math.signum(amount);
        return true;
    }
}
