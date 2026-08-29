package com.magicsmp.saveditems;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class MagicSavedItemsMod implements ModInitializer {
    public static final String MOD_ID = "magicsaveditems";

    public static final ResourceKey<CreativeModeTab> SAVED_ITEMS_TAB_KEY = ResourceKey.create(
            BuiltInRegistries.CREATIVE_MODE_TAB.key(),
            Identifier.fromNamespaceAndPath(MOD_ID, "saved_items")
    );

    public static final CreativeModeTab SAVED_ITEMS_TAB = FabricCreativeModeTab.builder()
            .title(Component.literal("Saved Items"))
            .icon(() -> SavedItemStore.getSavedItems().values().stream()
                    .findFirst()
                    .map(ItemStack::copy)
                    .orElseGet(() -> new ItemStack(Items.CHEST)))
            .displayItems((parameters, output) -> {
                for (ItemStack stack : SavedItemStore.getSavedItems().values()) {
                    output.accept(stack.copy());
                }
            })
            .build();

    @Override
    public void onInitialize() {
        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                SAVED_ITEMS_TAB_KEY,
                SAVED_ITEMS_TAB
        );
    }
}
