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
| Tough As Nails | Its temperature decides cold and heat; its thirst slows or speeds up recovery |
| Droplets of Thirst | Being thirsty slows recovery, being well quenched speeds it up; regenerating can cost thirst |
| Serene Seasons | Winter outdoors is cold, summer sun is hot |
| Curios | The Feather Ring goes in a ring slot |
| Jade | Shows a mount's stamina when you look at it |
| AppleSkin, Overflowing Bars | Sit nicely alongside the feathers |

On Minecraft 1.21.1 and older, also:

| Mod | What it does with feathers |
|---|---|
| Cold Sweat | Your body temperature decides when you're cold or overheating |
| Legendary Survival Overhaul | The same as Tough As Nails, from its temperature and hydration (not on 1.19.2) |
| Thirst Was Taken | The same as Droplets of Thirst, for worlds still on the original mod (Droplets of Thirst is its maintained continuation) |

Those three have no build for Minecraft 26.x yet. [Minecraft Versions](Minecraft-Versions) lists what each version supports and was tested with.

## Versions

Feathers of Fatigue calls into these mods directly, so it accepts the versions it was tested with, up to the next major version. With a version outside that range, the game stops at load and names the mod and the range, instead of crashing later in the middle of play.

| Mod | Accepted versions (26.1) |
|---|---|
| Tough As Nails | 21.11.0.6 up to 22 |
| Droplets of Thirst | 26.1-1.0.0 up to 26.1-2 |
| Serene Seasons | 26.1.2.0.4 up to 26.1.3 |
| Curios | 15.0.0 up to 16 |
| Jade | 26.1.11 up to 26.2 |
| Overflowing Bars | 26.1.0 up to 26.2 |

Droplets of Thirst is accepted the same way on every Minecraft version: from `<minecraft>-1.0.0` up to `<minecraft>-2`, where `<minecraft>` is the version Feathers of Fatigue is made for (`1.21.1-1.0.0` up to `1.21.1-2` on 1.21.1).
