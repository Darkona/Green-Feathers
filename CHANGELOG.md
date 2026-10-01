# Changelog

This file follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and each change has its date.

Version 1.0.0 was released on 2026-10-01 for each Minecraft version as `<minecraft>-1.0.0`, for example `1.21.1-1.0.0`. On Minecraft 26.3 it is a beta, `26.3-1.0.0-beta.1`, because NeoForge 26.3 is still in beta. An entry that names a Minecraft version applies only to that version, or to the versions it names.

Planned: Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul on Minecraft 26.x, and Tough As Nails on 26.3, when they publish a build for it. Also a build for the NeoForge 26.3 releases after 26.3.0.36-beta, when Droplets of Thirst and the compats follow.

## [1.0.1] - 2026-10-01

### Added
- 2026-10-01: Another mod can take over player actions through the API: `FeathersAPI.takeOverPlayerActions(modId)`, called during mod construction or common setup, turns the basic exertion (the sprint and jump costs) off, so a player never pays twice for the same action. `FeathersAPI.arePlayerActionsTakenOver()` and `FeathersAPI.getPlayerActionOwners()` read it, and the comment of `basic_exertion_enabled` says so. Released as `<minecraft>-1.0.1` on each Minecraft version, except 26.1.2 (`26.1.2-1.0.2`, after the relabel below) and 26.3 (`26.3-1.0.0-beta.2`).

### Changed
- 2026-10-01: On 26.1.2, 26.1.2-1.0.2 requires NeoForge 26.1.2.109 or newer (it was 26.1.2.112), like Droplets of Thirst 26.1.2-1.0.2: KubeJS 8.0.6 only loads on NeoForge 26.1.2.109. It is built against Droplets of Thirst 26.1.2-1.0.2.
- 2026-10-01: On 26.1.2, the build is labeled with the Minecraft version it runs on: 26.1.2-1.0.1 is 26.1-1.0.0 under its new name, with the same features, config and API. It is built against Droplets of Thirst 26.1.2-1.0.1 and accepts Droplets of Thirst 26.1.2-1.0.0 up to 26.1.2-2. The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.1.2-1.0.1`.

## [1.0.0] - 2026-10-01

### Added
- 2026-09-30: Minecraft 1.21.11 build (1.21.11-1.0.0), on NeoForge 21.11.45 or later, built from 26.1-1.0.0 with the same features, config and API: armor is recognized by its equipment, copper armor weighs 1, and the API has `Identifier` in every signature. The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:1.21.11-1.0.0`.
- 2026-09-30: On 1.21.11, works with Droplets of Thirst 1.21.11-1.0.0, Tough As Nails 21.11.0.4, Serene Seasons 21.11.0.5 with GlitchCore 21.11.0.4, Curios 14.0.0, Jade 21.1.7, AppleSkin 3.0.8 and Overflowing Bars 21.11.0 with Puzzles Lib 21.11.13. Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul, Naturalist and Mob Wrangler have no NeoForge build for 1.21.11, so their compats are left out, as on 26.1.
- 2026-09-27: Minecraft 26.3 build (26.3-1.0.0-beta.1), on NeoForge 26.3.0.36-beta, the same as 26.2-1.0.0 except for the 26.3 entries in this file.
- 2026-09-27: On 26.3, works with Droplets of Thirst 26.3-1.0.0-beta.1, Serene Seasons 26.1.2.0.7 (its build for 26.3, despite the number), Curios 17.0.0-beta.2, Jade 26.3.1, AppleSkin 3.0.10, Overflowing Bars 26.3.0 with Puzzles Lib 26.3.8, and Naturalist 2.0.6.
- 2026-09-25: Minecraft 26.2 build (26.2-1.0.0), on NeoForge 26.2.0.88 or later, the same as 26.1-1.0.0 except for the 26.2 entries in this file.
- 2026-09-25: On 26.2 and newer, Naturalist is back: its ostriches, giraffes and elephants get feathers again, as on 1.21.1.
- 2026-09-25: On 26.2, works with Droplets of Thirst 26.2-1.0.0, Tough As Nails 21.11.0.8, Serene Seasons 26.1.2.0.6 (its newest build for 26.2, despite the number), Curios 16.0.0, Jade 26.2.10, AppleSkin 3.0.10, Overflowing Bars 26.2.0 and Naturalist 2.0.6.
- 2026-09-24: Minecraft 26.1.2 build (26.1-1.0.0), on NeoForge 26.1.2.112 or later, with the same features, config and API as 1.21.1-1.0.0 except for the 26.1 entries in this file.
- 2026-09-24: On 26.1 and newer, copper armor and copper horse armor weigh 1 by default (`@minecraft:copper=1`, `@minecraft:copper/body=1`).
- 2026-09-24: On 26.1, works with Droplets of Thirst 26.1-1.0.0, Tough As Nails 21.11.0.6, Serene Seasons 26.1.2.0.4, Curios 15.0.0, Jade 26.1.11, AppleSkin 3.0.9 and Overflowing Bars 26.1.0.
- 2026-07-26: Each supported mod declares the versions it accepts. A version outside them stops the game at load with a message, instead of a crash in play.
- 2026-06-04: Every feather is drawn from grayscale sprites (body, half body, outline, shine, empty slot), tinted with a body color and an outline color, instead of a hand-drawn set per color. Each state keeps its own shape: crystal feathers when cold or with Momentum, a glint when energized, its own stripe for strain.
- 2026-06-04: Cold puts frost over the feathers, as in the first Green Feathers. The flames overlay is available to styles and resource packs.
- 2026-06-04: The feathers move like hearts: a wave while Energized, a shake when only a few are left, a pulse (or a shake) while strained. The client config (`[animations]`) sets each one, and also the low threshold.
- 2026-06-04: Resource packs can recolor any state, change its shape or overlay, or give it sprites of their own, with `assets/feathers_of_fatigue/feather_styles.json` (see the Resource Packs wiki page).
- 2026-06-04: Droplets of Thirst, the maintained continuation of Thirst Was Taken: thirst slows regeneration, being well quenched speeds it up, and regeneration can cost thirst (optional). Players with thirst turned off are not affected.
- 2026-06-04: Feather styles API (`com.darkona.feathersoffatigue.api.client`): register a `FeatherStyle` (body and border color, variant, overlay, optional sprites) and a `FeatherStyleProvider` that picks it for the player by condition, by priority. `FeatherVariants` adds shapes and overlays, from the 56x72 sheet layout of Feathers of Fatigue or from a texture of any size in multiples of 8. The states of Feathers of Fatigue use the same registry.
- 2026-06-04: Feather animations API: a `FeatherAnimationProvider` picks a wave, shake or pulse for the row (of the player or of their mount) by condition, by priority. The triggers of Feathers of Fatigue use the same path.
- 2026-05-15: **Food (optional):** a full food bar with saturation left makes regeneration faster, and hunger makes it slower (`saturation_regen_bonus`, `hunger_regen_penalty`). Off by default.
- 2026-05-10: Weight from other mods is drawn in its own color.
- 2026-05-10: Horse armor weighs a little.
- 2026-05-10: Game rules are in a server config, synced to clients. Modpacks ship defaults in `defaultconfigs/`.
- 2026-05-07: **Armor weight (optional):** every armor piece holds back feathers that you cannot use, drawn in the color of the piece, head to feet.
- 2026-05-07: **Mounts (optional):** horses, donkeys, mules and camels have their own feathers. While you ride, their feathers replace yours, in the colors of the animal.
- 2026-05-07: Galloping and jumping tire mounts. An exhausted mount slows down and cannot jump.
- 2026-05-07: Each animal is born with its own stamina, and foals take after their parents.
- 2026-05-07: A datapack sets which animals count as mounts, and their stats.
- 2026-05-07: Feathers after the first row stack in layers of stronger shades.
- 2026-05-07: Jade sits nicely next to the feathers.
- 2026-05-01: A rewrite of Green Feathers for NeoForge 1.21.1, with a new API.
- 2026-05-01: Feathers regenerate on their own, after a short pause once you spend some.
- 2026-05-01: **Strain:** when you run out, you can keep going into red strain feathers. Regeneration pays strain back first, and slowly.
- 2026-05-01: **Exhaustion:** if you spend absolutely everything, you cannot exert yourself until you catch your breath.
- 2026-05-01: **Resting:** standing still, crouching or sitting (boats, horses, most furniture seats) pays strain back faster. Sleeping through the night restores everything.
- 2026-05-01: Cold slows regeneration. Heat makes everything cost double. The Nether, fire and lava also cut your maximum feathers.
- 2026-05-01: Fire Resistance and the Potion of Cooling keep you fresh.
- 2026-05-01: Armor weights per item or material, set in the config or by datapack (data map).
- 2026-05-01: The Lightweight enchantment and the Feather Ring (Curios ring slot) make the armor lighter.
- 2026-05-01: Other mods can add weight (a backpack, a full inventory) through the API.
- 2026-05-01: Potions: Endurance (golden bonus feathers), Energy (faster regeneration), Momentum (cheaper actions), Cooling.
- 2026-05-01: HUD: feathers above the food bar, colored by state: normal, cold, hot, strained, energized, momentum, endurance.
- 2026-05-01: Armor tooltips show the weight of the item.
- 2026-05-01: Cold Sweat, Tough As Nails, Legendary Survival Overhaul: their body temperature decides cold and heat, and their thirst or hydration affects regeneration.
- 2026-05-01: Thirst Was Taken: the same, for packs still on the original mod.
- 2026-05-01: Serene Seasons: winter outdoors is cold, and the summer sun is hot.
- 2026-05-01: Curios: the Feather Ring goes in a ring slot.
- 2026-05-01: AppleSkin and Overflowing Bars sit nicely next to the feathers.
- 2026-05-01: The `/feathers` command reads and changes the feathers of a player.
- 2026-05-01: For mod developers, a separate API jar: spend, drain and read feathers, add regeneration factors and weight sources, and listen to events.

### Changed
- 2026-09-27: On 26.3, the brewing recipes are data, as Minecraft 26.3 does brewing: `data/feathers_of_fatigue/recipe/brewing/`, one `minecraft:brewing` recipe per mix and container (potion, splash, lingering) and per container change (gunpowder, dragon's breath), like the vanilla ones. The mixes are the same, and a datapack can now change or remove them.
- 2026-09-27: On 26.3, this build is for NeoForge 26.3.0.36-beta only, like Droplets of Thirst 26.3-1.0.0-beta.1 and the compat versions it works with (Puzzles Lib 26.3.9 needs a newer NeoForge). NeoForge 26.3 is still in beta, and 26.3.0.37-beta renames the config types, so mods built for one side of that change do not load on the other.
- 2026-09-27: On 26.3, the API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.3-1.0.0-beta.1`, with the same API as 26.1.
- 2026-09-25: On 26.2, the API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.2-1.0.0`, with the same API as 26.1.
- 2026-09-24: On 26.1 and newer, armor is recognized by its equipment (the `minecraft:equippable` component with an equipment model, worn in an armor slot or on the body of a mount), as Minecraft 26.1 defines armor, instead of by its item class. Armor from other mods counts if it has the same definition.
- 2026-09-24: On 26.1 and newer, material rules (`@minecraft:iron`, `@minecraft:iron/chestplate`) name the equipment model, which for vanilla armor is its material. The turtle shell is `@minecraft:turtle_scute` (it was `@minecraft:turtle`), and the default list follows.
- 2026-09-24: On 26.1, use Serene Seasons 26.1.2.0.4 to 26.1.2.0.6. Serene Seasons 26.1.2.0.7 crashes the client on its own as soon as a world renders.
- 2026-09-24: On 26.1 and newer, the API is the same, with the Minecraft renames: `ResourceLocation` is now `Identifier` in every signature. The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.1-1.0.0`.
- 2026-09-06: Green Feathers is now **Feathers of Fatigue**. Every id follows: the mod id and resource namespace are `feathers_of_fatigue` (attributes, effects, enchantments, tags, data maps, lang keys), the code is in `com.darkona.feathersoffatigue` (the API in `com.darkona.feathersoffatigue.api`), and the jars are `feathers-of-fatigue` and `feathers-of-fatigue-api` (group `com.darkona.feathersoffatigue`).
- 2026-09-06: The config files are `FeathersOfFatigue-Server.toml`, `FeathersOfFatigue-Compat.toml` and `FeathersOfFatigue-Client.toml`, in `serverconfig/feathers_of_fatigue/` and `config/feathers_of_fatigue/`. The `/feathers` command keeps its name.

### Removed
- 2026-09-27: On 26.3, the Tough As Nails compat and its config section: Tough As Nails has no build for Minecraft 26.3. Droplets of Thirst covers thirst, and Serene Seasons covers the cold and heat of the seasons.
- 2026-09-27: On 26.3, Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul and Mob Wrangler are still out, as on 26.2, because they have no build.
- 2026-09-25: On 26.2, Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul are still out, as on 26.1, because they have no build for Minecraft 26.2. Mob Wrangler has no build for Minecraft 26.2 either.
- 2026-09-24: On 26.1, the Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul compats and their config sections: none of these mods has a build for Minecraft 26.1. Droplets of Thirst and Tough As Nails cover thirst, and Tough As Nails covers temperature.
- 2026-09-24: On 26.1, Naturalist and Mob Wrangler mounts: these mods have no build for Minecraft 26.1. Their data map entries stay, and apply when the mods come.
- 2026-09-06: Worlds and configs from earlier builds do not carry over. Stored feathers, enchantments and items under the old ids are lost, and the game ignores the old config files.
- 2026-05-29: The old API of Elenai's Feathers (`com.elenai.feathers.api.FeathersHelper`). Use `FeathersAPI` on the server or `ClientFeathers` on the client.

### Fixed
- 2026-07-31: Sleeping through the night restores everything also when a sleep mod skips the night.
- 2026-07-29: Another Endurance potion extends what is left of the golden feathers, and a stronger one adds only its extra feathers.
- 2026-07-23: Leaving the bed before morning no longer restores feathers.
- 2026-06-04: An animal that stops counting as a mount (after a config or datapack change) drops its strain, exhaustion and slowdown.
