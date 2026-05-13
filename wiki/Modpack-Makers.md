# Modpack Makers

Everything here works with a datapack; no code needed.

## Defaults for your pack

Put your versions of `Feathers-Server.toml` and `Feathers-Compat.toml` in `defaultconfigs/feathers/`. New worlds and
servers start from them. See [Configuration](Configuration) for every option.

## Mounts

Horses, donkeys, mules and camels have feathers when `mounts_enabled` is on. Any other creature can be a mount with
a data map entry in `data/greenfeathers/data_maps/entity_type/mount_stats.json`, optionally with its own numbers
(anything left out uses the config):

```json
{
  "values": {
    "mymod:dragon": { "min_feathers": 40, "max_feathers": 60, "gallop_feathers_per_second": 0.05 }
  }
}
```

| Field | Meaning |
|---|---|
| `min_feathers`, `max_feathers` | The range each animal's stamina is rolled in when it spawns |
| `regen_feathers_per_second` | How fast it recovers |
| `gallop_feathers_per_second` | What galloping costs |
| `gallop_speed` | How fast counts as galloping (horizontal blocks per tick) |
| `jump_feathers` | What a full-power jump costs |

The `greenfeathers:mounts` entity type tag also works, with the config's numbers.

Giving a creature feathers doesn't make it rideable: it only matters for creatures players can already ride.

Green Feathers ships entries for mounts from several mods (Naturalist, Alex's Mobs, Mob Wrangler, Sniff Trail,
Wyrmroost, and others), each only active when its mod is installed.

### Opting out

Entity types in the `greenfeathers:no_feathers` tag never have feathers, even horses: useful for special mounts
that shouldn't tire.

```json
{ "values": ["mymod:spirit_horse"] }
```

in `data/greenfeathers/tags/entity_type/no_feathers.json`.

## Armor weights

Weights are looked up in this order: an item rule, a tag rule, the `greenfeathers:armor_weight` data map, a
material-and-piece rule, a material rule, and finally the armor's defense points times
`unlisted_armor_weight_per_defense`. The rules live in `armor_weights` in the server config:

```toml
armor_weights = [
    "minecraft:netherite_chestplate=5",   # one item
    "#c:armors/heavy=4",                  # an item tag
    "@minecraft:iron/chestplate=3",       # one piece of a material (helmet, chestplate, leggings, boots, body)
    "@minecraft:iron=2"                   # every piece of a material
]
```

`body` is horse armor. The data map sets weights per item from a datapack, in
`data/greenfeathers/data_maps/item/armor_weight.json`:

```json
{
  "values": {
    "mymod:plate_armor_chestplate": 6
  }
}
```
