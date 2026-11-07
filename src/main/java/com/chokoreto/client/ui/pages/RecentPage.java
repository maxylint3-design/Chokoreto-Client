package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Format;
import com.chokoreto.client.Stacks;
import com.chokoreto.client.Store;
import com.chokoreto.client.ui.ClientScreen;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** History of your finished auctions, with an "Auction again" button on each. */
public class RecentPage extends Page {
    private static final int ROW_H = 24;
    private int scroll = 0;

    private int visibleRows() {
        return (h - 30) / ROW_H;
    }

    @Override
    public void build() {
        ws.clear();
        ws.add(new W.Btn(x + w - 70, y, 70, 14, "Clear all", () -> {
            Store.get().auctions.clear();
            Store.save();
            scroll = 0;
        }));
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        List<Store.AuctionRecord> list = Store.get().auctions;
        title(g, "Recent Auctions", "Your last " + list.size() + " finished auctions.");

        if (list.isEmpty()) {
            card(g, x, y + 26, w, h - 30);
            Ui.textCenter(g, "No auctions yet", x + w / 2, y + 80, Ui.TEXT);
            Ui.textCenter(g, "Finished auctions show up here.", x + w / 2, y + 94, Ui.MUTED);
            return;
        }

        int rows = visibleRows();
        scroll = Math.max(0, Math.min(scroll, Math.max(0, list.size() - rows)));
        for (int i = 0; i < rows && scroll + i < list.size(); i++) {
            Store.AuctionRecord r = list.get(scroll + i);
            int ry = y + 28 + i * ROW_H;
            Ui.round(g, x, ry, w, ROW_H - 2, Ui.SURFACE | 0xFF000000);

            ItemStack stack = Stacks.stackOf(r.itemId);
            ClientScreen.drawItem(g, stack, x + 5, ry + 3);

            Ui.text(g, Ui.fit(r.count + "x " + r.name, 150), x + 28, ry + 3, Ui.TEXT);
            String result = r.winner == null ? "No bids" : "Sold " + Format.money(r.price) + " to " + r.winner;
            Ui.text(g, Ui.fit(result, 170), x + 28, ry + 13, r.winner == null ? Ui.MUTED : Ui.GREEN);
            Ui.textRight(g, Format.ago(r.endedAt), x + w - 74, ry + 8, Ui.MUTED);

            int bx = x + w - 66;
            boolean hover = Ui.in(mx, my, bx, ry + 3, 62, 16);
            Ui.round(g, bx, ry + 3, 62, 16, (hover ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
            Ui.textCenter(g, "Re-list", bx + 31, ry + 7, Ui.accent());
        }
    }

    @Override
    public boolean click(double mx, double my) {
        List<Store.AuctionRecord> list = Store.get().auctions;
        int rows = visibleRows();
        for (int i = 0; i < rows && scroll + i < list.size(); i++) {
            int ry = y + 28 + i * ROW_H;
            if (Ui.in(mx, my, x + w - 66, ry + 3, 62, 16)) {
                Store.AuctionRecord r = list.get(scroll + i);
                ItemStack stack = Stacks.stackOf(r.itemId);
                if (!stack.isEmpty()) {
                    s.auctionPage().prefill(stack, r.count, r.minBid);
                    s.open(ClientScreen.Tab.AUCTION);
                }
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
