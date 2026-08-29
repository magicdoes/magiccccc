package com.magicsmp.saveditems;

import net.minecraft.world.item.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public final class SavedItemStore {
    private static final Map<String, ItemStack> SAVED_ITEMS = new LinkedHashMap<>();

    private SavedItemStore() {
    }

    public static Map<String, ItemStack> getSavedItems() {
        return SAVED_ITEMS;
    }
}
