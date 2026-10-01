# Changelog

Changes by feature, newest version first.

## 26.3-1.0.0-beta.1 (NeoForge), 2026-10-01

Feathers of Fatigue for Minecraft 26.3, on NeoForge 26.3.0.36-beta. Same as 26.2-1.0.0 below, except for what this section lists.

### Potions

- The brewing recipes are data now, as Minecraft 26.3 does brewing: `data/feathers_of_fatigue/recipe/brewing/`, one `minecraft:brewing` recipe per mix and container (potion, splash, lingering) and per container change (gunpowder, dragon's breath), like vanilla's. The mixes are the same; a datapack can now change or remove them.

### Compatibility

- Tested with Droplets of Thirst 26.3-1.0.0-beta.1, Serene Seasons 26.1.2.0.7 (its build for 26.3, despite the number), Curios 17.0.0-beta.2, Jade 26.3.1, AppleSkin 3.0.10, Overflowing Bars 26.3.0 with Puzzles Lib 26.3.8, and Naturalist 2.0.6.
- NeoForge 26.3 is still in beta, and 26.3.0.37-beta renames the config types: mods built for one side of that change do not load on the other. This build, like Droplets of Thirst 26.3-1.0.0-beta.1, is for 26.3.0.36-beta only, and so are the compat versions above (Puzzles Lib 26.3.9 needs a newer NeoForge).

### For mod developers

- The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.3-1.0.0-beta.1`, with the same API as 26.1.

### Not in this version

- **Tough As Nails:** no build for Minecraft 26.3; its compat and config section are left out. Droplets of Thirst covers thirst, Serene Seasons the seasons' cold and heat.
- **Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul, Mob Wrangler:** still no build, as on 26.2.

## 26.2-1.0.0 (NeoForge), 2026-10-01

Feathers of Fatigue for Minecraft 26.2, on NeoForge 26.2.0.88 or later. Same as 26.1-1.0.0 below, except for what this section lists.

### Compatibility

- Naturalist is back: its ostriches, giraffes and elephants get feathers again, as on 1.21.1.
- Tested with Droplets of Thirst 26.2-1.0.0, Tough As Nails 21.11.0.8, Serene Seasons 26.1.2.0.6 (its newest build for 26.2, despite the number), Curios 16.0.0, Jade 26.2.10, AppleSkin 3.0.10, Overflowing Bars 26.2.0 and Naturalist 2.0.6.

### For mod developers

- The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.2-1.0.0`, with the same API as 26.1.

### Not in this version

- **Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul:** still no build for Minecraft 26.2; their compats and config sections are left out, as on 26.1.
- **Mob Wrangler:** no build for Minecraft 26.2.

## 26.1.2-1.0.1 (NeoForge), 2026-10-01

The Minecraft 26.1.2 build is now labeled 26.1.2, the Minecraft version it runs on. Same features, config and API as 26.1-1.0.0 below.

- Built against Droplets of Thirst 26.1.2-1.0.1, and accepts Droplets of Thirst 26.1.2-1.0.0 up to 26.1.2-2.
- The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.1.2-1.0.1`.

## 26.1-1.0.0 (NeoForge), 2026-10-01

Feathers of Fatigue for Minecraft 26.1.2, on NeoForge 26.1.2.112 or later. Same features, config and API as 1.21.1-1.0.0 below, except for what this section lists.

### Armor weight

- Armor is recognized by its equipment (the `minecraft:equippable` component with an equipment model, worn in an armor slot or on a mount's body), the way Minecraft 26.1 defines armor, instead of by its item class. Armor from other mods counts as long as it is defined that way.
- Material rules (`@minecraft:iron`, `@minecraft:iron/chestplate`) name the equipment model, which for vanilla armor is its material. The turtle shell's is `@minecraft:turtle_scute` (it was `@minecraft:turtle`), and the default list follows.
- Copper armor and copper horse armor weigh 1 by default (`@minecraft:copper=1`, `@minecraft:copper/body=1`).

### Compatibility

- Tested with Droplets of Thirst 26.1-1.0.0, Tough As Nails 21.11.0.6, Serene Seasons 26.1.2.0.4, Curios 15.0.0, Jade 26.1.11, AppleSkin 3.0.9 and Overflowing Bars 26.1.0.
- Serene Seasons 26.1.2.0.7 crashes the client on its own as soon as a world renders; use 26.1.2.0.4 to 26.1.2.0.6.

### For mod developers

- The API is the same, with Minecraft's renames: `ResourceLocation` is now `Identifier` in every signature. The API jar is `com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.1-1.0.0`.

### Not in this version

- **Cold Sweat, Thirst Was Taken, Legendary Survival Overhaul:** none of them has a build for Minecraft 26.1. Their compats and config sections are left out; Droplets of Thirst and Tough As Nails cover thirst, and Tough As Nails temperature.
- **Naturalist, Mob Wrangler:** no build for Minecraft 26.1, so their mounts are not there to get feathers. The data map entries for them stay, and apply when the mods come.

## 1.21.1-1.0.0 (NeoForge), 2026-10-01

A rewrite of Green Feathers for NeoForge 1.21.1 (21.1.252 or later), with a new API.

### Renamed to Feathers of Fatigue

- Green Feathers is now **Feathers of Fatigue**. Every id follows: the mod id and resource namespace are `feathers_of_fatigue` (attributes, effects, enchantments, tags, data maps, lang keys), the code lives in `com.darkona.feathersoffatigue` (the API in `com.darkona.feathersoffatigue.api`), and the jars are `feathers-of-fatigue` and `feathers-of-fatigue-api` (group `com.darkona.feathersoffatigue`).
- Config files are `FeathersOfFatigue-Server.toml`, `FeathersOfFatigue-Compat.toml` and `FeathersOfFatigue-Client.toml`, in `serverconfig/feathers_of_fatigue/` and `config/feathers_of_fatigue/`. The `/feathers` command keeps its name.
- Worlds and configs from earlier builds do not carry over: stored feathers, enchantments and items under the old ids are lost, and the old config files are ignored.

### Stamina

- Feathers regenerate on their own after a short pause once you spend some.
- **Strain:** when you run out you can keep going into red strain feathers. Regeneration pays strain back first, slowly.
- **Exhaustion:** spend absolutely everything and you can't exert yourself until you catch your breath.
- **Resting:** standing still, crouching or sitting (boats, horses, most furniture seats) pays strain back faster. Sleeping through the night restores everything, also when a sleep mod skips the night; leaving the bed before morning does not.
- **Food (optional):** a full food bar with saturation left speeds regeneration up, hunger slows it down (`saturation_regen_bonus`, `hunger_regen_penalty`). Off by default.

### Weather and climate

- Cold slows regeneration. Heat makes everything cost double; the Nether, fire and lava also cut your maximum feathers.
- Fire Resistance and the Potion of Cooling keep you fresh.

### Armor weight (optional)

- Every armor piece holds back feathers you can't use, drawn in the piece's own color, head to feet.
- Weights per item or material, set in the config or by datapack (data map).
- The Lightweight enchantment and the Feather Ring (Curios ring slot) lighten the load.
- Other mods can add weight (a backpack, a full inventory) through the API, drawn in their own color.

### Mounts (optional)

- Horses, donkeys, mules and camels have their own feathers, shown instead of yours while you ride, in the animal's colors.
- Galloping and jumping tire them; an exhausted mount slows down and can't jump.
- An animal that stops counting as a mount (config or datapack change) drops its strain, exhaustion and slowdown.
- Each animal is born with its own stamina, and foals take after their parents.
- Horse armor weighs a little.
- Which animals count, and their stats, set by datapack.

### Potions

- Endurance (golden bonus feathers; another potion extends what is left, a stronger one adds its extra feathers), Energy (faster regeneration), Momentum (cheaper actions), Cooling.

### HUD

- Feathers above the food bar, colored by state: normal, cold, hot, strained, energized, momentum, endurance.
- Feathers beyond one row stack in layers of stronger shades.
- Weight feathers drawn in each source's color.
- Every feather is drawn from grayscale sprites (body, half body, outline, shine, empty slot) tinted with a body and an outline color, instead of a hand-drawn set per color. Each state keeps its own shape: crystal feathers when cold or with Momentum, a glint when energized, its own stripe for strain.
- Cold puts frost over the feathers, as in the first Green Feathers. The flames overlay is available to styles and resource packs.
- The feathers move like hearts: a wave while Energized, a shake when only a few are left, a pulse (or a shake) while strained. Each is set in the client config (`[animations]`), and the low threshold too.
- Resource packs can recolor any state, change its shape or overlay, or give it sprites of their own, with `assets/feathers_of_fatigue/feather_styles.json` (see the Resource Packs wiki page).
- Armor tooltips show the item's weight.

### Compatibility

- Cold Sweat, Tough As Nails, Legendary Survival Overhaul: their body temperature decides cold and heat; their thirst or hydration affects regeneration.
- Droplets of Thirst, the maintained continuation of Thirst Was Taken: thirst slows regeneration, being well quenched speeds it up, and regenerating can cost thirst (optional). Players with thirst turned off are not affected.
- Thirst Was Taken: the same, for packs still on the original mod.
- Serene Seasons: winter outdoors is cold, summer sun is hot.
- Curios: the Feather Ring goes in a ring slot.
- AppleSkin, Overflowing Bars, Jade: sit nicely alongside the feathers.
- Each supported mod declares the versions it accepts; a version outside them stops the game at load with a message instead of a crash in play.

### Configuration

- Game rules live in a server config, synced to clients; modpacks ship defaults in `defaultconfigs/`.
- The `/feathers` command reads and changes a player's feathers.

### For mod developers

- A separate API jar: spend, drain and read feathers, add regeneration factors and weight sources, and listen to events.
- Feather styles (`com.darkona.feathersoffatigue.api.client`): register a `FeatherStyle` (body and border color, variant, overlay, optional sprites) and a `FeatherStyleProvider` that picks it for the player by condition, by priority. New shapes and overlays with `FeatherVariants`, from Feathers of Fatigue's 56x72 sheet layout or a texture of any size in multiples of 8. Feathers of Fatigue's own states use the same registry.
- Feather animations: a `FeatherAnimationProvider` picks a wave, shake or pulse for the row (the player's or their mount's) by condition, by priority. Feathers of Fatigue's own triggers use the same path.
- The old API of Elenai's Feathers (`com.elenai.feathers.api.FeathersHelper`) is gone: use `FeathersAPI` on the server or `ClientFeathers` on the client.

## Planned

- Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul on Minecraft 26.x, and Tough As Nails on 26.3, once they publish a build for it.
- A build for the NeoForge 26.3 releases after 26.3.0.36-beta, once Droplets of Thirst and the compats follow.
