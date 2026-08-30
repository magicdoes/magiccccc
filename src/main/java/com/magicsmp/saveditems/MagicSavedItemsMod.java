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

import java.util.ArrayList;
import java.util.List;

public class MagicSavedItemsMod implements ModInitializer {

    public static final String MOD_ID = "magicsaveditems";

    public static final ResourceKey<CreativeModeTab> SAVED_ITEMS_TAB_KEY =
            ResourceKey.create(
                    BuiltInRegistries.CREATIVE_MODE_TAB.key(),
                    Identifier.fromNamespaceAndPath(
                            MOD_ID,
                            "saved_items"
                    )
            );

    public static final CreativeModeTab SAVED_ITEMS_TAB =
            FabricCreativeModeTab.builder()

                    // ==========================================
                    // TAB NAME
                    // ==========================================

                    .title(
                            Component.literal("Saved Items")
                    )

                    // ==========================================
                    // TAB ICON
                    // ==========================================

                    .icon(() -> {

                        ItemStack icon =
                                SavedItemStore
                                        .getSavedItems()
                                        .values()
                                        .stream()
                                        .findFirst()
                                        .map(ItemStack::copy)
                                        .orElseGet(
                                                () -> new ItemStack(Items.CHEST)
                                        );

                        // Creative tab icon must be one item
                        icon.setCount(1);

                        return icon;
                    })

                    // ==========================================
                    // TAB CONTENTS
                    // ==========================================

                    .displayItems((parameters, output) -> {

                        List<ItemStack> addedStacks =
                                new ArrayList<>();

                        for (
                                ItemStack savedStack :
                                SavedItemStore
                                        .getSavedItems()
                                        .values()
                        ) {

                            // ==========================================
                            // SKIP EMPTY / INVALID ITEMS
                            // ==========================================

                            if (
                                    savedStack == null ||
                                    savedStack.isEmpty()
                            ) {
                                continue;
                            }

                            // ==========================================
                            // CREATE DISPLAY COPY
                            // ==========================================

                            ItemStack displayStack =
                                    savedStack.copy();

                            /*
                             * Minecraft 26.2 requires items being
                             * registered in a Creative tab to have
                             * a stack size of exactly 1.
                             *
                             * This only changes the displayed copy.
                             * The actual saved ItemStack is untouched.
                             */
                            displayStack.setCount(1);

                            // ==========================================
                            // DUPLICATE PROTECTION
                            // ==========================================

                            boolean duplicate = false;

                            for (
                                    ItemStack existing :
                                    addedStacks
                            ) {

                                if (
                                        ItemStack.isSameItemSameComponents(
                                                existing,
                                                displayStack
                                        )
                                ) {

                                    duplicate = true;
                                    break;
                                }
                            }

                            // ==========================================
                            // ADD TO SAVED ITEMS TAB
                            // ==========================================

                            if (!duplicate) {

                                addedStacks.add(
                                        displayStack.copy()
                                );

                                output.accept(
                                        displayStack
                                );
                            }
                        }
                    })

                    .build();

    // ==========================================
    // INITIALIZE MOD
    // ==========================================

    @Override
    public void onInitialize() {

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                SAVED_ITEMS_TAB_KEY,
                SAVED_ITEMS_TAB
        );
    }
}
