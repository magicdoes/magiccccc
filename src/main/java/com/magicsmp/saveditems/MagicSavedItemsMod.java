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

                        // Creative tab icons should always be 1 item
                        icon.setCount(1);

                        return icon;
                    })

                    // ==========================================
                    // SAVED ITEMS
                    // ==========================================

                    .displayItems((parameters, output) -> {

                        List<ItemStack> added =
                                new ArrayList<>();

                        for (
                                ItemStack savedStack :
                                SavedItemStore
                                        .getSavedItems()
                                        .values()
                        ) {

                            // Ignore invalid/empty entries
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
                             * Minecraft Creative tabs require
                             * every displayed ItemStack to have
                             * a count of exactly 1.
                             *
                             * This ONLY changes the Creative-tab copy.
                             * It does NOT modify the saved item.
                             */
                            displayStack.setCount(1);

                            // ==========================================
                            // DUPLICATE CHECK
                            // ==========================================

                            boolean duplicate = false;

                            for (
                                    ItemStack existing :
                                    added
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
                            // ADD TO CREATIVE TAB
                            // ==========================================

                            if (!duplicate) {

                                added.add(
                                        displayStack.copy()
                                );

                                output.accept(
                                        displayStack
                                );
                            }
                        }
                    })

                    .build();

    }

    @Override
    public void onInitialize() {

        Registry.register(
                BuiltInRegistries.CREATIVE_MODE_TAB,
                SAVED_ITEMS_TAB_KEY,
                SAVED_ITEMS_TAB
        );
    }
}
