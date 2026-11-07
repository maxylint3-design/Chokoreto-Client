package com.chokoreto.client.ui.pages;

import com.chokoreto.client.Config;
import com.chokoreto.client.Format;
import com.chokoreto.client.Store;
import com.chokoreto.client.Transactions;
import com.chokoreto.client.ui.ClientScreen;
import com.chokoreto.client.ui.HudEditorScreen;
import com.chokoreto.client.ui.Page;
import com.chokoreto.client.ui.Ui;
import com.chokoreto.client.ui.W;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;

import java.util.List;

/** Money you got and sent in chat, with filters, session profit and the profit HUD settings. */
public class TransactionsPage extends Page {
    private static final int ROW_H = 13;

    private long minFilter = 0;
    private String minText = "";
    private int type = 0; // 0 all, 1 received, 2 sent
    private String playerQuery = "";
    private int scroll = 0;
    private EditBox minBox;

    @Override
    public void build() {
        ws.clear();

        // clear history
        ws.add(new W.Btn(x + w - 80, y, 80, 14, "Clear History", () -> {
            Transactions.clearHistory();
            scroll = 0;
        }));

        // profit display row
        int py = y + 66;
        ws.add(new W.Toggle(x + 8, py, 96, 16, "On screen", () -> Config.get().profitShow, v -> Config.get().profitShow = v));
        ws.add(new W.Toggle(x + 112, py, 80, 16, "Skin", () -> Config.get().profitSkin, v -> Config.get().profitSkin = v));
        ws.add(new W.Btn(x + w - 86, py, 80, 16, "Edit position",
                () -> Minecraft.getInstance().gui.setScreen(new HudEditorScreen(s))));

        // minimum filter
        int fy = y + 94;
        minBox = s.edit(x + 48, fy, 70, 16, "Any, e.g. 5m", minText, 12, t -> {
            minText = t;
            minFilter = t.isBlank() ? 0 : Math.max(0, Format.parseMoney(t));
            scroll = 0;
        });

        String[] labels = {"Any", "100k+", "1m+", "5m+", "10m+"};
        long[] values = {0, 100_000L, 1_000_000L, 5_000_000L, 10_000_000L};
        for (int i = 0; i < labels.length; i++) {
            long v = values[i];
            ws.add(new W.Btn(x + 124 + i * 34, fy, 32, 16, labels[i], () -> {
                minFilter = v;
                minText = v == 0 ? "" : Format.shortNumber(v);
                scroll = 0;
                s.rebuild();
            }, () -> minFilter == v, null));
        }

        // type + player filters
        int ty = y + 114;
        String[] types = {"All", "Received", "Sent"};
        int[] widths = {34, 56, 40};
        int tx = x + 48;
        for (int i = 0; i < 3; i++) {
            int n = i;
            ws.add(new W.Btn(tx, ty, widths[i], 16, types[i], () -> {
                type = n;
                scroll = 0;
            }, () -> type == n, null));
            tx += widths[i] + 2;
        }
        s.edit(x + w - 90, ty, 90, 16, "Player", playerQuery, 17, t -> {
            playerQuery = t;
            scroll = 0;
        });
    }

    @Override
    public void draw(GuiGraphicsExtractor g, int mx, int my) {
        Store store = Store.get();
        Ui.text(g, "Transactions", x, y, Ui.TEXT);
        Ui.text(g, String.valueOf(store.txs.size()), x + 70, y, Ui.accent());
        Ui.text(g, "Money you got and sent in chat.", x, y + 11, Ui.MUTED);

        // three cards
        int cw = (w - 12) / 3;
        stat(g, x, y + 24, cw, "RECEIVED", "+" + Format.money(Transactions.totalReceived()), Ui.GREEN);
        stat(g, x + cw + 6, y + 24, cw, "SENT", "-" + Format.money(Transactions.totalSent()), Ui.RED);

        long net = Transactions.sessionNet();
        int sx = x + (cw + 6) * 2;
        card(g, sx, y + 24, cw, 36);
        Ui.roundOutline(g, sx, y + 24, cw, 36, Ui.alpha(Ui.GREEN, 90));
        Ui.text(g, "SESSION · " + Format.ago(store.sessionStart), sx + 6, y + 29, Ui.MUTED);
        Ui.text(g, Format.signed(net), sx + 6, y + 43, net < 0 ? Ui.RED : Ui.GREEN);
        boolean hover = Ui.in(mx, my, sx + cw - 38, y + 40, 32, 14);
        Ui.round(g, sx + cw - 38, y + 40, 32, 14, (hover ? Ui.SURFACE3 : Ui.SURFACE2) | 0xFF000000);
        Ui.textCenter(g, "Reset", sx + cw - 22, y + 43, Ui.TEXT);

        // profit row background
        card(g, x, y + 64, w, 20);

        Ui.text(g, "Minimum", x, y + 98, Ui.MUTED);

        // list
        List<Store.Tx> list = Transactions.filtered(minFilter, type, playerQuery);
        int listY = y + 136;
        int rows = Math.max(1, (h - 136) / ROW_H);
        scroll = Math.max(0, Math.min(scroll, Math.max(0, list.size() - rows)));

        card(g, x, listY - 2, w, h - 134);
        if (list.isEmpty()) {
            Ui.textCenter(g, "No transactions yet", x + w / 2, listY + 16, Ui.TEXT);
            Ui.textCenter(g, "When someone pays you, or you pay someone, it shows up here.", x + w / 2, listY + 28, Ui.MUTED);
            return;
        }
        for (int i = 0; i < rows && scroll + i < list.size(); i++) {
            Store.Tx tx = list.get(scroll + i);
            int ry = listY + i * ROW_H;
            Ui.text(g, tx.received ? "+" + Format.money(tx.amount) : "-" + Format.money(tx.amount),
                    x + 8, ry + 2, tx.received ? Ui.GREEN : Ui.RED);
            Ui.text(g, (tx.received ? "from " : "to ") + tx.player, x + 90, ry + 2, Ui.TEXT);
            Ui.textRight(g, Format.ago(tx.time), x + w - 8, ry + 2, Ui.MUTED);
        }
    }

    private void stat(GuiGraphicsExtractor g, int sx, int sy, int cw, String label, String value, int color) {
        card(g, sx, sy, cw, 36);
        Ui.text(g, label, sx + 6, sy + 5, Ui.MUTED);
        Ui.text(g, value, sx + 6, sy + 19, color);
    }

    @Override
    public boolean click(double mx, double my) {
        int cw = (w - 12) / 3;
        int sx = x + (cw + 6) * 2;
        if (Ui.in(mx, my, sx + cw - 38, y + 40, 32, 14)) {
            Transactions.resetSession();
            return true;
        }
        return false;
    }

    @Override
    public boolean scroll(double amount) {
        scroll -= (int) Math.signum(amount);
        return true;
    }
}
