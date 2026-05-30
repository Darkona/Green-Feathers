# Changelog

Changes by feature, newest version first.

## 2.0.0 (Minecraft 1.18.2, Forge 40), unreleased

The Green Feathers rewrite of 1.21.1, ported to Forge 1.18.2 (through the 1.20.1 and 1.19.2 ports) with the same features, config options and API. It replaces the 1.3.0 code of this branch, and its API. Needs Forge 40.2.3 or later.

### Stamina

- Feathers regenerate on their own after a short pause once you spend some.
- When you run out you can keep going into red strain feathers. Regeneration pays strain back first, slowly.
- **Exhaustion:** spend absolutely everything and you can't exert yourself until you catch your breath.
- **Resting:** standing still, crouching or sitting (boats, horses, most furniture seats) pays strain back faster. Sleeping through the night restores everything.
- **Food (optional):** a full food bar with saturation left speeds regeneration up, hunger slows it down (`saturation_regen_bonus`, `hunger_regen_penalty`). Off by default.
- On its own, sprinting and jumping cost feathers; Actions of Stamina takes over player actions when installed.

### Weather and climate

- Cold slows regeneration. Heat makes everything cost double; the Nether, fire and lava also cut your maximum feathers.
- Fire Resistance and the Potion of Cooling keep you fresh.

### Armor weight (optional)

- Every armor piece holds back feathers you can't use, drawn in the piece's own color (leather in its dye), head to feet.
- Weights per item, tag or material, set in the config or by datapack in `data/greenfeathers/data_maps/item/armor_weight.json`.
- Horse armor weighs too. It has no armor material in 1.18.2, so material rules match its texture name: `horse_armor_iron` is `@minecraft:iron/body`.
- The Lightweight enchantment and the Feather Ring (Curios ring slot, or the off hand without Curios) lighten the load; the Curse of Heaviness doubles it.
- Other mods can add weight (a backpack, a full inventory) through the API, drawn in their own color.

### Mounts (optional)

- Horses, donkeys and mules have their own feathers, shown instead of yours while you ride, in the animal's colors.
- Galloping and jumping tire them; an exhausted mount slows down and can't jump.
- Each animal is born with its own stamina, and foals take after their parents.
- Which animals count, and their stats, set by datapack in `data/greenfeathers/data_maps/entity_type/mount_stats.json` and the `greenfeathers:mounts` and `greenfeathers:no_feathers` entity type tags (`data/greenfeathers/tags/entity_types/`).

### Potions

- Endurance (golden bonus feathers), Energy (faster regeneration), Momentum (cheaper actions), Cooling, Coldness and Heat.

### HUD

- Feathers above the food bar, colored by state: normal, cold, hot, strained, energized, momentum, endurance. Green, blue or white, your choice.
- Feathers beyond one row stack in layers of stronger shades.
- Weight feathers drawn in each source's color.
- Armor and horse armor tooltips show the item's weight.

### Compatibility

Each one only does something when its mod is installed, and can be turned off in the config. Tested with these 1.18.2 Forge builds:

- Cold Sweat 2.4.3: body temperature decides cold and heat.
- Tough As Nails 7.0.0.73: its temperature decides cold and heat; its thirst affects regeneration.
- Thirst Was Taken 1.18.2-1.3.11: thirst slows regeneration, being well quenched speeds it up.
- Serene Seasons 7.0.0.15: winter outdoors is cold, summer sun is hot.
- Curios 1.18.2-5.0.9.2: the Feather Ring goes in a ring slot.
- Jade 5.3.2: looking at a mount shows its stamina (it can be turned off in Jade's plugin settings).
- AppleSkin 2.5.1, Overflowing Bars 3.0.0: sit nicely alongside the feathers.
- Mounts from other mods get feathers when they are horses (tested with Naturalist 1.1.1's zebra), or when the mount stats data map or the `greenfeathers:mounts` tag lists them.

### Configuration

- Game rules live in server configs (`serverconfig/feathers/Feathers-Server.toml` and `Feathers-Compat.toml`), synced to clients; modpacks ship defaults in `defaultconfigs/feathers/`. HUD options are in `config/feathers/Feathers-Client.toml`. Option names are the same as in 1.21.1, without the Legendary Survival Overhaul section.
- The `/feathers` command reads and changes a player's or a mount's feathers.

### For mod developers

- A separate API jar, `greenfeathers-api`: spend, drain and read feathers, add regeneration factors, climate providers, stamina modifiers and weight sources, and listen to events.
- The old API of Elenai's Feathers (`com.elenai.feathers.api.FeathersHelper`) is gone: use `FeathersAPI` on the server or `ClientFeathers` on the client.
- The same API as 1.21.1, with the changes Forge 1.18.2 needs (the same as on 1.19.2 and 1.20.1, plus one):
  - Attributes, effects and enchantments are `RegistryObject`s (`FeathersAttributes.MAX_FEATHERS.get()`), not `DeferredHolder`s.
  - Events go on `MinecraftForge.EVENT_BUS`. `SpendEvent.Post#getSpendResult` replaces `getResult`, which Forge's `Event` already has.
  - `FeathersDataMaps` holds the data map ids and reads them: `FeathersDataMaps.armorWeight(item)`, `FeathersDataMaps.mountStats(type)`.
  - Attribute modifier operations have their 1.18.2 names: a Feather Ring is a `MULTIPLY_BASE` modifier of -0.5 on `greenfeathers:armor_weight_multiplier`.
  - Events are Forge 40 `LivingEvent`s: `getEntityLiving()` gives the entity, as `getEntity()` does on 1.19.2 and later.

### Not in this version

- **Camels:** Minecraft 1.18.2 has no camels, so there is nothing to give feathers to (and no camel dash to tire).
- **Legendary Survival Overhaul:** there is no 1.18.2 build, so its compatibility and its config section are left out.
- **Mob Wrangler:** there is no Forge 1.18.2 build, so its rideable creatures get no feathers. Its entry in the mount stats data map stays inactive.
- **Sniffers:** 1.18.2 has no sniffers, so the mount stats entry for them (used with Sniff Trail) is gone.
- **Infinite effects:** 1.18.2 has no infinite effect durations. Cold, Heat, Fatigue and Strained last for years while their cause lasts, and the inventory shows `**:**` as their time.
- **Enchantments from datapacks:** 1.18.2 has no data-driven enchantments. Lightweight and the Curse of Heaviness are defined in code: datapacks can't change their levels, costs or the items they apply to.
- **Naturalist ostriches, giraffes and elephants:** Naturalist 1.1.1, its last 1.18.2 build, has no ostriches, and its giraffes and elephants can't be ridden, so their mount stats entries are gone. Its zebras are horses and have feathers.
- **New mounts after `/reload`:** Forge 1.18.2 attaches feathers to a creature when it is created. A creature type that a reload turns into a mount gets its feathers only after its chunk reloads (or the server restarts).

## Planned

- The wiki gets a section per Minecraft version where the versions differ.
