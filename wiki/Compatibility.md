# Compatibility

Feathers of Fatigue stacks neatly with the other bars on the right: food, air bubbles, thirst.

| ![With Thirst Was Taken, Cold Sweat and AppleSkin](images/compat-thirst.png) | ![With Tough As Nails, Serene Seasons and AppleSkin](images/compat-tan.png) |
|---|---|
| Thirst Was Taken, Cold Sweat and AppleSkin | Tough As Nails, Serene Seasons and AppleSkin |
| ![Underwater in iron armor](images/underwater.png) | ![Jade showing a horse's stamina](images/jade.png) |
| Underwater in iron armor: feathers above the air bubbles | Jade shows a mount's stamina |

Supported out of the box, each with its own switch in `FeathersOfFatigue-Compat.toml`:

| Mod | What it does with feathers |
|---|---|
| Droplets of Thirst | Being thirsty slows recovery, being well quenched speeds it up; regenerating can cost thirst |
| Serene Seasons | Winter outdoors is cold, summer sun is hot |
| Curios | The Feather Ring goes in a ring slot |
| Jade | Shows a mount's stamina when you look at it |
| AppleSkin, Overflowing Bars | Sit nicely alongside the feathers |

On older Minecraft versions, also:

| Mod | What it does with feathers |
|---|---|
| Tough As Nails (26.2 and older) | Its temperature decides cold and heat; its thirst slows or speeds up recovery |
| Cold Sweat (1.21.1 and older) | Your body temperature decides when you're cold or overheating |
| Legendary Survival Overhaul (1.21.1 and older, not 1.19.2) | The same as Tough As Nails, from its temperature and hydration |
| Thirst Was Taken (1.21.1 and older) | The same as Droplets of Thirst, for worlds still on the original mod (Droplets of Thirst is its maintained continuation) |

They have no build for the newer versions yet. [Minecraft Versions](Minecraft-Versions) lists what each version supports and was tested with.

## Versions

Feathers of Fatigue calls into these mods directly, so it accepts the versions it was tested with, up to the next major version. With a version outside that range, the game stops at load and names the mod and the range, instead of crashing later in the middle of play.

| Mod | Accepted versions (26.3) |
|---|---|
| Droplets of Thirst | 26.3-1.0.0 up to 26.3-2 |
| Serene Seasons | 26.1.2.0.7 up to 26.1.3 (its builds for 26.x keep 26.1 numbers) |
| Curios | 17.0.0-beta.2 up to 18 |
| Jade | 26.3.1 up to 26.4 |
| Overflowing Bars | 26.3.0 up to 26.4 |

Droplets of Thirst is accepted the same way on every Minecraft version: from `<minecraft>-1.0.0` up to `<minecraft>-2`, where `<minecraft>` is the version Feathers of Fatigue is made for (`1.21.1-1.0.0` up to `1.21.1-2` on 1.21.1).
