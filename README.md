# Feathers of Fatigue

Feathers of Fatigue adds stamina to Minecraft, as a row of feathers above your food bar. Sprinting, jumping and whatever other mods choose cost feathers. When they run out, you can push on for a while, at a price.

**This build is for Minecraft 26.3 on NeoForge 26.3.0.36-beta.** Based on Elenai's Feathers. Builds for other Minecraft versions are listed on [Minecraft Versions](https://github.com/Darkona/feathers-of-fatigue/wiki/Minecraft-Versions).

This version needs exactly NeoForge 26.3.0.36-beta. NeoForge 26.3 is in beta, and 26.3.0.37-beta renames the config types, so mods built for one side of that change do not load on the other.

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

| ![Jade showing a horse's stamina](wiki/images/jade.png) | ![Riding a camel](wiki/images/mount-camel.png) |
|---|---|
| Jade shows the stamina of a mount | Riding a camel: its feathers, in its colors |

These mods work out of the box. Each one has its own switch in the config:

| Mod | What it does with feathers |
|---|---|
| Droplets of Thirst | Thirst slows recovery, and being well quenched speeds it up. Regeneration can cost thirst |
| Serene Seasons | Winter outdoors is cold, and the summer sun is hot |
| Curios | The Feather Ring goes in a ring slot |
| Jade | Shows the stamina of a mount when you look at it |
| AppleSkin, Overflowing Bars | Sit nicely next to the feathers |

Tough As Nails, Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul have no build for Minecraft 26.3, so this version does not support them.

## Documentation

The [wiki](https://github.com/Darkona/feathers-of-fatigue/wiki) has the details:

- [How It Plays](https://github.com/Darkona/feathers-of-fatigue/wiki/How-It-Plays): every mechanic, with the numbers.
- [Configuration](https://github.com/Darkona/feathers-of-fatigue/wiki/Configuration): every option, and the `/feathers` command.
- [Modpack Makers](https://github.com/Darkona/feathers-of-fatigue/wiki/Modpack-Makers): mounts, armor weights and opt-outs with datapacks.
- [Mod Developers](https://github.com/Darkona/feathers-of-fatigue/wiki/Mod-Developers): the API, to spend feathers from your own mod.

The wiki describes this version, Minecraft 26.3.

## License

[GNU GPL v3](LICENSE).
