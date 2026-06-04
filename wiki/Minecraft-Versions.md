# Minecraft Versions

This wiki describes the newest version of Green Feathers, for **Minecraft 1.21.1 (NeoForge)**. The same mod is also made for older Minecraft versions, with the same features, config options and API wherever the game allows it. This page lists only what is different in each of them.

| Minecraft | Loader | Download | Differences |
|---|---|---|---|
| 1.21.1 | NeoForge 21.1 | Newest | None: this whole wiki |
| 1.20.1 | Forge 47 | Supported | [1.20.1](#1201-forge-47) |
| 1.19.2 | Forge 43 | Supported | [1.19.2](#1192-forge-43) |
| 1.18.2 | Forge 40.2.4 or later | Supported | [1.18.2](#1182-forge-40) |

Files and folders are the same in every version: `serverconfig/feathers/Feathers-Server.toml` and `Feathers-Compat.toml` for the game rules (synced to clients), `config/feathers/Feathers-Client.toml` for the HUD, `defaultconfigs/feathers/` for modpack defaults.

## 1.20.1 (Forge 47)

**Missing:**

- **Mob Wrangler:** there is no Forge 1.20.1 build, so its rideable creatures get no feathers.
- **Enchantments from datapacks:** 1.20.1 has no data-driven enchantments. Lightweight and the Curse of Heaviness are defined in code; datapacks can't change their levels, costs or the items they apply to.
- **New mounts after `/reload`:** a creature gets its feathers when it is created. A creature type that a reload turns into a mount gets them after its chunk reloads, or after a restart.

**Tested with:** Cold Sweat 2.4.3.2, Tough As Nails 9.2, Legendary Survival Overhaul 2.4.7, Thirst Was Taken 1.4.0, Serene Seasons 9.1, Curios 5.14.1, Jade 11.13.3, AppleSkin 2.5.1, Overflowing Bars 8.0.1, Naturalist 5.0.

## 1.19.2 (Forge 43)

**Missing:** everything missing in 1.20.1, and:

- **Camels and sniffers:** they don't exist in 1.19.2.
- **Legendary Survival Overhaul:** there is no 1.19.2 build; its config section is left out.
- **Infinite effects:** 1.19.2 has no infinite durations. Cold, Heat, Fatigue and Strained last for years while their cause lasts, and the inventory shows `**:**`.

**Tested with:** Cold Sweat 2.4.3, Tough As Nails 8.0, Thirst Was Taken 1.3.11, Serene Seasons 8.1, Curios 5.1.6, Jade 8.9.2, AppleSkin 2.4.2, Overflowing Bars 4.0.1, Naturalist 4.0.3.

## 1.18.2 (Forge 40)

Needs Forge 40.2.4 or later.

**Missing:** everything missing in 1.19.2, and:

- **Naturalist ostriches, giraffes and elephants:** Naturalist 1.1.1, its last 1.18.2 build, has no ostriches, and its giraffes and elephants can't be ridden. Its zebras are horses and have feathers.

**Tested with:** Cold Sweat 2.4.3, Tough As Nails 7.0, Thirst Was Taken 1.3.11, Serene Seasons 7.0, Curios 5.0.9, Jade 5.3.2, AppleSkin 2.5.1, Overflowing Bars 3.0.0, Naturalist 1.1.1.

Mod developers: the API differences on older versions are in [Mod Developers](Mod-Developers#older-minecraft-versions).
