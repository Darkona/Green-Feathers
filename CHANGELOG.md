# Changelog

Changes by feature, newest version first.

## 2.0.0 (Minecraft 1.21.1, NeoForge), unreleased

A rewrite of Green Feathers for NeoForge 1.21.1, with a new API.

### Stamina

- Feathers regenerate on their own after a short pause once you spend some.
- **Strain:** when you run out you can keep going into red strain feathers. Regeneration pays strain back first, slowly.
- **Exhaustion:** spend absolutely everything and you can't exert yourself until you catch your breath.
- **Resting:** standing still, crouching or sitting (boats, horses, most furniture seats) pays strain back faster. Sleeping through the night restores everything.
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
- Each animal is born with its own stamina, and foals take after their parents.
- Horse armor weighs a little.
- Which animals count, and their stats, set by datapack.

### Potions

- Endurance (golden bonus feathers), Energy (faster regeneration), Momentum (cheaper actions), Cooling.

### HUD

- Feathers above the food bar, colored by state: normal, cold, hot, strained, energized, momentum, endurance.
- Feathers beyond one row stack in layers of stronger shades.
- Weight feathers drawn in each source's color.
- Armor tooltips show the item's weight.

### Compatibility

- Cold Sweat, Tough As Nails, Legendary Survival Overhaul: their body temperature decides cold and heat; their thirst or hydration affects regeneration.
- Thirst Was Taken: thirst slows regeneration, being well quenched speeds it up.
- Serene Seasons: winter outdoors is cold, summer sun is hot.
- Curios: the Feather Ring goes in a ring slot.
- AppleSkin, Overflowing Bars, Jade: sit nicely alongside the feathers.

### Configuration

- Game rules live in a server config, synced to clients; modpacks ship defaults in `defaultconfigs/`.
- The `/feathers` command reads and changes a player's feathers.

### For mod developers

- A separate API jar: spend, drain and read feathers, add regeneration factors and weight sources, and listen to events.

## Planned

- Ports of this version to Minecraft 1.20.1 (Forge 47), 1.19.2 (Forge 43) and 1.18.2 (Forge 40), each with the latest stable versions of the supported mods.
- The wiki gets a section per Minecraft version where the versions differ.
