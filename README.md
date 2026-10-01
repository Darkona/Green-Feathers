# Feathers of Fatigue

Feathers of Fatigue adds stamina to Minecraft, as a row of feathers above your food bar. Sprinting, jumping and whatever other mods choose cost feathers. When they run out, you can push on for a while, at a price.

**This build is for Minecraft 1.21.1 on NeoForge 21.1 or later.** Based on Elenai's Feathers. Builds for other Minecraft versions are listed on [Minecraft Versions](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions).

![Feathers above the food bar, with a golden row of Endurance feathers](wiki/images/hud.png)

## How it plays

- **Regeneration**: feathers come back on their own, a little every second. After you spend some, they wait a short time first.
- **Strain**: when you run out, you can keep going into red *strain* feathers. Regeneration pays strain back first, and slowly. If you overdo it, you stay drained for a while.
- **Rest**: standing still, crouching or sitting down (a boat, a horse, or most seats from furniture mods) pays strain back faster. Sleep restores everything.
- **Exhaustion**: if you spend absolutely everything, you are exhausted. You cannot exert yourself again until you catch your breath.
- **Weather and climate**: cold weather slows your recovery. Heat makes everything cost double. The Nether, fire and lava also cut your maximum feathers. Fire Resistance or a Potion of Cooling keeps you fresh.
- **Heavy armor** (optional): every piece holds back some feathers that you cannot use, drawn in the color of that piece, head to feet. Netherite is heavy. The *Lightweight* enchantment and the *Feather Ring* help.
- **Mounts** (optional): horses, donkeys, mules and camels have their own feathers. While you ride, their feathers replace yours, in the colors of the animal. Galloping and jumping tire them slowly. An exhausted mount slows down and cannot jump. Horse armor also weighs a little. Each animal is born with its own stamina, like speed and health, and foals take after their parents.
- **Potions**: Endurance (golden bonus feathers), Energy (faster recovery), Momentum (cheaper actions) and Cooling.

## Plays well with others

The feathers stack neatly with the other bars on the right: food, air bubbles, thirst.

| ![With Thirst Was Taken, Cold Sweat and AppleSkin](wiki/images/compat-thirst.png) | ![Riding a camel](wiki/images/mount-camel.png) |
|---|---|
| With Thirst Was Taken, Cold Sweat and AppleSkin | Riding a camel: its feathers, in its colors |

These mods work out of the box. Each one has its own switch in the config:

| Mod | What it does with feathers |
|---|---|
| Cold Sweat | Your body temperature decides when you are cold or overheating |
| Tough As Nails | Its temperature decides cold and heat. Its thirst slows or speeds up recovery |
| Legendary Survival Overhaul | The same as Tough As Nails, from its temperature and hydration |
| Droplets of Thirst | Thirst slows recovery, and being well quenched speeds it up. Regeneration can cost thirst |
| Thirst Was Taken | The same as Droplets of Thirst, for worlds still on the original mod (Droplets of Thirst is its maintained continuation) |
| Serene Seasons | Winter outdoors is cold, and the summer sun is hot |
| Curios | The Feather Ring goes in a ring slot |
| Jade | Shows the stamina of a mount when you look at it |
| AppleSkin, Overflowing Bars | Sit nicely next to the feathers |

## Documentation

The [wiki](https://github.com/Darkona/feathers-of-fatigue/wiki) has the details:

- [How It Plays](https://github.com/Darkona/feathers-of-fatigue/wiki/How-It-Plays): every mechanic, with the numbers.
- [Configuration](https://github.com/Darkona/feathers-of-fatigue/wiki/Configuration): every option, and the `/feathers` command.
- [Modpack Makers](https://github.com/Darkona/feathers-of-fatigue/wiki/Modpack-Makers): mounts, armor weights and opt-outs with datapacks.
- [Mod Developers](https://github.com/Darkona/feathers-of-fatigue/wiki/Mod-Developers): the API, to spend feathers from your own mod.

The wiki describes the newest version, for Minecraft 26.3. [Minecraft Versions](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions#1211-neoforge-211) lists what is different on 1.21.1.

## License

[GNU GPL v3](LICENSE).
