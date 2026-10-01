# Minecraft Versions

This wiki describes Feathers of Fatigue for **Minecraft 26.1.2 (NeoForge)**. The same mod is also made for other Minecraft versions, with the same features, config options and API wherever the game allows it. This page lists only what is different in each of them.

| Minecraft | Loader | Download | Differences |
|---|---|---|---|
| 26.3 | NeoForge 26.3.0.36-beta only | [26.3-1.0.0-beta.1](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v26.3-1.0.0-beta.1) (beta) | [On the wiki](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions) |
| 26.2 | NeoForge 26.2.0.88 or later | [26.2-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v26.2-1.0.0) | [On the wiki](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions) |
| 26.1.2 | NeoForge 26.1.2.112 or later | [26.1.2-1.0.1](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v26.1.2-1.0.1) | None: this whole wiki |
| 1.21.11 | NeoForge 21.11.45 or later | [1.21.11-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v1.21.11-1.0.0) | [On the wiki](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions) |
| 1.21.1 | NeoForge 21.1.252 or later | [1.21.1-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v1.21.1-1.0.0) | [1.21.1](#1211-neoforge-211) |
| 1.20.1 | Forge 47.4.10 or later | [1.20.1-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v1.20.1-1.0.0) | [1.20.1](#1201-forge-47) |
| 1.19.2 | Forge 43.5.2 or later | [1.19.2-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v1.19.2-1.0.0) | [1.19.2](#1192-forge-43) |
| 1.18.2 | Forge 40.3.12 or later | [1.18.2-1.0.0](https://github.com/Darkona/feathers-of-fatigue/releases/tag/v1.18.2-1.0.0) | [1.18.2](#1182-forge-40) |

Files and folders are the same in every version: `serverconfig/feathers_of_fatigue/FeathersOfFatigue-Server.toml` and `FeathersOfFatigue-Compat.toml` for the game rules (synced to clients), `config/feathers_of_fatigue/FeathersOfFatigue-Client.toml` for the HUD, `defaultconfigs/feathers_of_fatigue/` for modpack defaults.

## 26.1.2 (NeoForge 26.1.2)

**Missing:**

- **Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul:** none has a build for Minecraft 26.1, so their compats and config sections are left out.
- **Naturalist, Mob Wrangler:** no build for Minecraft 26.1, so their mounts don't exist.

**Tested with:** Droplets of Thirst 26.1.2-1.0.1, Tough As Nails 21.11.0.6, Serene Seasons 26.1.2.0.4, Curios 15.0.0, Jade 26.1.11, AppleSkin 3.0.9, Overflowing Bars 26.1.0. Serene Seasons 26.1.2.0.7 crashes the client on its own; use 26.1.2.0.4 to 26.1.2.0.6.

## 1.21.1 (NeoForge 21.1)

**Has, on top of 26.1.2:** Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul (with their sections in `FeathersOfFatigue-Compat.toml`, see [Configuration](Configuration#default-feathersoffatigue-compattoml)), and Naturalist and Mob Wrangler mounts.

**Different:**

- **Armor materials:** material rules name the armor material, so the turtle shell is `@minecraft:turtle`. There is no copper armor, and the default `armor_weights` list has no copper entries.
- **API:** ids are `ResourceLocation`.

**Tested with:** Cold Sweat 2.4.3.1, Tough As Nails 10.1.0.13, Legendary Survival Overhaul 2.4.7.2, Droplets of Thirst 1.21.1-1.0.0, Thirst Was Taken 1.21.1-2.1.5, Serene Seasons 10.1.0.9, Curios 9.5.1, Jade 15.10.6, Overflowing Bars 21.1.1, Naturalist and Mob Wrangler.

## 1.20.1 (Forge 47)

**Missing, compared with 1.21.1:**

- **Mob Wrangler:** there is no Forge 1.20.1 build, so its rideable creatures get no feathers.
- **Enchantments from datapacks:** 1.20.1 has no data-driven enchantments. Lightweight and the Curse of Heaviness are defined in code; datapacks can't change their levels, costs or the items they apply to.
- **New mounts after `/reload`:** a creature gets its feathers when it is created. A creature type that a reload turns into a mount gets them after its chunk reloads, or after a restart.

**Tested with:** Cold Sweat 2.4.3.2, Tough As Nails 9.2, Legendary Survival Overhaul 2.4.7, Droplets of Thirst 1.20.1-1.0.0, Thirst Was Taken 1.4.0, Serene Seasons 9.1, Curios 5.14.1, Jade 11.13.3, AppleSkin 2.5.1, Overflowing Bars 8.0.1, Naturalist 5.0.

## 1.19.2 (Forge 43)

**Missing:** everything missing in 1.20.1, and:

- **Camels and sniffers:** they don't exist in 1.19.2.
- **Legendary Survival Overhaul:** there is no 1.19.2 build; its config section is left out.
- **Infinite effects:** 1.19.2 has no infinite durations. Cold, Heat, Fatigue and Strained last for years while their cause lasts, and the inventory shows `**:**`.

**Tested with:** Cold Sweat 2.4.3, Tough As Nails 8.0, Droplets of Thirst 1.19.2-1.0.0, Thirst Was Taken 1.3.11, Serene Seasons 8.1, Curios 5.1.6, Jade 8.9.2, AppleSkin 2.4.2, Overflowing Bars 4.0.1, Naturalist 4.0.3.

## 1.18.2 (Forge 40)

Needs Forge 40.3.12 or later.

**Missing:** everything missing in 1.19.2, and:

- **Naturalist ostriches, giraffes and elephants:** Naturalist 1.1.1, its last 1.18.2 build, has no ostriches, and its giraffes and elephants can't be ridden. Its zebras are horses and have feathers.

**Tested with:** Cold Sweat 2.4.3, Tough As Nails 7.0, Droplets of Thirst 1.18.2-1.0.0, Thirst Was Taken 1.3.11, Serene Seasons 7.0, Curios 5.0.9, Jade 5.3.2, AppleSkin 2.5.1, Overflowing Bars 3.0.0, Naturalist 1.1.1.

Mod developers: the API differences on older versions are in [Mod Developers](Mod-Developers#older-minecraft-versions).
