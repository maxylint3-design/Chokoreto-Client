package com.chokoreto.client;

import net.minecraft.world.item.ItemStack;

/** Everything about ONE running (or just-finished) auction. */
public final class Auction {
    public final ItemStack stack;
    public final int count;
    public final long minBid;
    public final long endTimeMs;
    public final boolean announce;

    public long highestBid = 0;
    public String highestBidder = null;
    public boolean finished = false;
    public long finishedAtMs = 0;

    public Auction(ItemStack stack, int count, long minBid, long durationSeconds, boolean announce) {
        this.stack = stack.copyWithCount(1);
        this.count = count;
        this.minBid = minBid;
        this.endTimeMs = System.currentTimeMillis() + durationSeconds * 1000L;
        this.announce = announce;
    }

    public long millisLeft() {
        return Math.max(0, endTimeMs - System.currentTimeMillis());
    }

    /** "1x Elytra" */
    public String title() {
        return count + "x " + stack.getHoverName().getString();
    }
}
