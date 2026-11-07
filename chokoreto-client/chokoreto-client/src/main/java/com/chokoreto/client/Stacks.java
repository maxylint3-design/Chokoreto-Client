package com.chokoreto.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Turns items into text ids ("minecraft:elytra") and back, so we can save them in files. */
public final class Stacks {
    private Stacks() {}

    public static String idOf(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    public static ItemStack stackOf(String id) {
        if (id == null) return ItemStack.EMPTY;
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null) return ItemStack.EMPTY;
        Item item = BuiltInRegistries.ITEM.getValue(identifier);
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(item);
    }
}
