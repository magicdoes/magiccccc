# Magic Saved Items — Fabric 26.2

Client-side Fabric mod for Minecraft Java 25.2.

## What it does

- `/saveitem <name>` saves an exact copy of the item in your **main hand**.
- Preserves the ItemStack data/components handled by Minecraft's ItemStack codec, including things such as:
  - enchantments
  - custom name
  - lore
  - damage/durability
  - attributes/components
  - custom model/item data stored on the stack
  - stack count
- Adds a **Saved Items** Creative inventory tab.
- `/deleteitem <name>` removes a saved preset.
- `/saveditems` lists saved presets.
- `/reloadsaveditems` reloads the JSON file.

Saved data is stored client-side in:

`config/magicsaveditems/saved_items.json`

## Important

This mod only saves a local item preset and exposes it in Creative inventory. The server still decides what inventory actions/items it accepts. It does not grant Creative permissions or bypass server permissions.

## Build requirements

- Java 25
- Minecraft 26.2
- Fabric Loader 0.19.3+
- Fabric API 0.156.0+26.2

Build with Gradle/Loom and put the resulting JAR in your client `mods` folder alongside Fabric API.


## One-click build

You do **not** need to install Gradle manually.

Windows:

`gradlew.bat build`

Linux/macOS:

`./gradlew build`

The launcher downloads Gradle 9.2.1 automatically the first time. You still need Java 25 installed and selected as your active Java.

The finished mod JAR will be in `build/libs/`.
