package com.magicsmp.saveditems;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MagicSavedItemsClient implements ClientModInitializer {

    public static final String MOD_ID = "magicsaveditems";

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path SAVE_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("magicsaveditems")
            .resolve("saved_items.json");

    @Override
    public void onInitializeClient() {

        // ==========================================
        // LOAD ITEMS WHEN JOINING
        // ==========================================

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            loadSavedItems();
        });

        // ==========================================
        // COMMANDS
        // ==========================================

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext) -> {

            // ==========================================
            // /saveitem <name>
            // ==========================================

            dispatcher.register(
                    ClientCommands.literal("saveitem")
                            .then(
                                    ClientCommands.argument(
                                                    "name",
                                                    StringArgumentType.greedyString()
                                            )
                                            .executes(context -> {

                                                String name =
                                                        StringArgumentType.getString(
                                                                context,
                                                                "name"
                                                        ).trim();

                                                return saveHeldItem(
                                                        name,
                                                        context.getSource()
                                                );
                                            })
                            )
            );

            // ==========================================
            // /deleteitem <name>
            // ==========================================

            dispatcher.register(
                    ClientCommands.literal("deleteitem")
                            .then(
                                    ClientCommands.argument(
                                                    "name",
                                                    StringArgumentType.greedyString()
                                            )
                                            .executes(context -> {

                                                String name =
                                                        StringArgumentType.getString(
                                                                context,
                                                                "name"
                                                        ).trim();

                                                return deleteSavedItem(
                                                        name,
                                                        context.getSource()
                                                );
                                            })
                            )
            );

            // ==========================================
            // /saveditems
            // ==========================================

            dispatcher.register(
                    ClientCommands.literal("saveditems")
                            .executes(context -> {

                                if (SavedItemStore.getSavedItems().isEmpty()) {

                                    context.getSource().sendFeedback(
                                            Component.literal(
                                                    "§eNo saved items yet."
                                            )
                                    );

                                    return 1;
                                }

                                context.getSource().sendFeedback(
                                        Component.literal(
                                                "§bSaved Items §7(" +
                                                        SavedItemStore
                                                                .getSavedItems()
                                                                .size() +
                                                        "):"
                                        )
                                );

                                for (
                                        String name :
                                        SavedItemStore
                                                .getSavedItems()
                                                .keySet()
                                ) {

                                    context.getSource().sendFeedback(
                                            Component.literal(
                                                    "§7- §f" + name
                                            )
                                    );
                                }

                                return 1;
                            })
            );

            // ==========================================
            // /reloadsaveditems
            // ==========================================

            dispatcher.register(
                    ClientCommands.literal("reloadsaveditems")
                            .executes(context -> {

                                loadSavedItems();
                                refreshSavedItemsTab();

                                context.getSource().sendFeedback(
                                        Component.literal(
                                                "§aReloaded §f" +
                                                        SavedItemStore
                                                                .getSavedItems()
                                                                .size() +
                                                        " §asaved item(s)."
                                        )
                                );

                                return 1;
                            })
            );
        });
    }

    // ==========================================
    // SAVE HELD ITEM
    // ==========================================

    private static int saveHeldItem(
            String name,
            net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source
    ) {

        Minecraft minecraft = Minecraft.getInstance();

        if (
                minecraft.player == null ||
                minecraft.level == null
        ) {

            source.sendError(
                    Component.literal(
                            "You must be in a world/server."
                    )
            );

            return 0;
        }

        if (name.isBlank()) {

            source.sendError(
                    Component.literal(
                            "Usage: /saveitem <name>"
                    )
            );

            return 0;
        }

        ItemStack held =
                minecraft.player.getMainHandItem();

        if (held.isEmpty()) {

            source.sendError(
                    Component.literal(
                            "Hold an item in your main hand first."
                    )
            );

            return 0;
        }

        // ==========================================
        // COPY THE EXACT HELD ITEM
        // ==========================================

        ItemStack savedCopy = held.copy();

        // ==========================================
        // RENAME ONLY THE SAVED COPY
        // ==========================================

        savedCopy.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        // ==========================================
        // STORE THE SAVED COPY
        // ==========================================

        SavedItemStore
                .getSavedItems()
                .put(
                        name,
                        savedCopy
                );

        // ==========================================
        // WRITE TO DISK
        // ==========================================

        if (!writeSavedItems()) {

            source.sendError(
                    Component.literal(
                            "The item was stored in memory, " +
                                    "but the save file could not be written."
                    )
            );

            return 0;
        }

        // Refresh Saved Items tab if open
        refreshSavedItemsTab();

        source.sendFeedback(
                Component.literal(
                        "§aSaved §f" +
                                name +
                                "§a."
                )
        );

        return 1;
    }

    // ==========================================
    // DELETE SAVED ITEM
    // ==========================================

    private static int deleteSavedItem(
            String name,
            net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource source
    ) {

        if (
                SavedItemStore
                        .getSavedItems()
                        .remove(name)
                        == null
        ) {

            source.sendError(
                    Component.literal(
                            "No saved item named \"" +
                                    name +
                                    "\"."
                    )
            );

            return 0;
        }

        if (!writeSavedItems()) {

            source.sendError(
                    Component.literal(
                            "Removed from memory, " +
                                    "but the save file could not be written."
                    )
            );

            return 0;
        }

        refreshSavedItemsTab();

        source.sendFeedback(
                Component.literal(
                        "§cDeleted §f" +
                                name +
                                "§c."
                )
        );

        return 1;
    }

    // ==========================================
    // SAFE CREATIVE TAB REFRESH
    // ==========================================

    private static void refreshSavedItemsTab() {

        Minecraft minecraft = Minecraft.getInstance();

        if (
                minecraft.player == null ||
                minecraft.level == null
        ) {
            return;
        }

        try {

            if (
                    minecraft.gui.screen()
                            instanceof CreativeModeInventoryScreen creativeScreen
            ) {

                FabricCreativeModeInventoryScreen fabricScreen =
                        (FabricCreativeModeInventoryScreen) creativeScreen;

                if (
                        fabricScreen.getSelectedTab()
                                == MagicSavedItemsMod.SAVED_ITEMS_TAB
                ) {

                    fabricScreen.setSelectedTab(
                            MagicSavedItemsMod.SAVED_ITEMS_TAB
                    );
                }
            }

        } catch (Exception exception) {

            System.err.println(
                    "[MagicSavedItems] Could not refresh Saved Items tab."
            );

            exception.printStackTrace();
        }
    }

    // ==========================================
    // WRITE SAVED ITEMS
    // ==========================================

    private static boolean writeSavedItems() {

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return false;
        }

        try {

            Files.createDirectories(
                    SAVE_FILE.getParent()
            );

            RegistryOps<JsonElement> ops =
                    RegistryOps.create(
                            JsonOps.INSTANCE,
                            minecraft.level.registryAccess()
                    );

            JsonObject root =
                    new JsonObject();

            for (
                    Map.Entry<String, ItemStack> entry :
                    SavedItemStore
                            .getSavedItems()
                            .entrySet()
            ) {

                JsonElement encoded =
                        ItemStack.CODEC
                                .encodeStart(
                                        ops,
                                        entry.getValue()
                                )
                                .getOrThrow();

                root.add(
                        entry.getKey(),
                        encoded
                );
            }

            Files.writeString(
                    SAVE_FILE,
                    GSON.toJson(root),
                    StandardCharsets.UTF_8
            );

            return true;

        } catch (Exception exception) {

            exception.printStackTrace();

            return false;
        }
    }

    // ==========================================
    // LOAD SAVED ITEMS
    // ==========================================

    private static void loadSavedItems() {

        Minecraft minecraft = Minecraft.getInstance();

        SavedItemStore
                .getSavedItems()
                .clear();

        if (
                minecraft.level == null ||
                !Files.exists(SAVE_FILE)
        ) {
            return;
        }

        try {

            RegistryOps<JsonElement> ops =
                    RegistryOps.create(
                            JsonOps.INSTANCE,
                            minecraft.level.registryAccess()
                    );

            JsonElement fileJson =
                    GSON.fromJson(
                            Files.readString(
                                    SAVE_FILE,
                                    StandardCharsets.UTF_8
                            ),
                            JsonElement.class
                    );

            if (
                    fileJson == null ||
                    !fileJson.isJsonObject()
            ) {
                return;
            }

            for (
                    Map.Entry<String, JsonElement> entry :
                    fileJson
                            .getAsJsonObject()
                            .entrySet()
            ) {

                try {

                    ItemStack stack =
                            ItemStack.CODEC
                                    .parse(
                                            ops,
                                            entry.getValue()
                                    )
                                    .getOrThrow();

                    if (!stack.isEmpty()) {

                        SavedItemStore
                                .getSavedItems()
                                .put(
                                        entry.getKey(),
                                        stack
                                );
                    }

                } catch (Exception ignored) {

                    System.err.println(
                            "[MagicSavedItems] " +
                                    "Could not load saved item: " +
                                    entry.getKey()
                    );
                }
            }

        } catch (IOException exception) {

            exception.printStackTrace();
        }
    }
}
