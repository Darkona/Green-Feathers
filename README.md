# Green Feathers

**Stamina for Minecraft, as a row of feathers above your food bar.** Sprinting, jumping, and whatever other mods
decide cost feathers. Run out and you can push on for a while, at a price.

Minecraft 1.21.1 · NeoForge · based on Elenai's Feathers

![Feathers above the food bar, with a golden row of Endurance feathers](wiki/images/hud.png)

## How it plays

- **Feathers come back on their own**, a little every second, after a short pause once you've spent some.
- **Push past empty.** When you run out you can keep going into red *strain* feathers. Regeneration pays strain
  back before anything else, slowly, so overdoing it leaves you drained for a while.
- **Rest to recover.** Standing still, crouching or sitting down (a boat, a horse, or most seats from furniture mods)
  pays strain back faster. Sleep restores everything.
- **Exhaustion.** Spend absolutely everything and you're exhausted: no exerting yourself until you've caught your
  breath.
- **Weather and climate matter.** Cold weather slows your recovery. Heat makes everything cost double, and the
  Nether, fire and lava also cut your maximum feathers. Fire Resistance or a Potion of Cooling keeps you fresh.
- **Heavy armor weighs you down** (optional). Every piece holds back some feathers you can't use, shown in that
  piece's own color, head to feet. Netherite is heavy; the *Lightweight* enchantment and the *Feather Ring* help.
- **Mounts tire too** (optional). Horses, donkeys, mules and camels have their own feathers, shown instead of yours
  while you ride, in the colors of the animal you're on. Galloping and jumping tire them slowly; an exhausted mount
  slows down and can't jump. Horse armor weighs a little too. Like speed and health, each animal is born with its
  own stamina, and foals take after their parents.
- **Potions:** Endurance (golden bonus feathers), Energy (faster recovery), Momentum (cheaper actions), Cooling.

On its own, Green Feathers makes sprinting and jumping cost feathers. Install
[Actions of Stamina](https://github.com/Darkona/actions-of-stamina) for attacks, elytra, swimming, shields and
movement mods like ParCool, Paragliders and Better Combat.

## Plays well with others

It stacks neatly with the other bars on the right: food, air bubbles, thirst.

| ![With Thirst Was Taken, Cold Sweat and AppleSkin](wiki/images/compat-thirst.png) | ![Riding a camel](wiki/images/mount-camel.png) |
|---|---|
| With Thirst Was Taken, Cold Sweat and AppleSkin | Riding a camel: its feathers, in its colors |

Supported out of the box, each switchable in the config:

| Mod | What it does with feathers |
|---|---|
| Cold Sweat | Your body temperature decides when you're cold or overheating |
| Tough As Nails | Its temperature decides cold and heat; its thirst slows or speeds up recovery |
| Legendary Survival Overhaul | The same, from its temperature and hydration |
| Thirst Was Taken | Being thirsty slows recovery, being well quenched speeds it up |
| Serene Seasons | Winter outdoors is cold, summer sun is hot |
| Curios | The Feather Ring goes in a ring slot |
| AppleSkin, Overflowing Bars | Sit nicely alongside the feathers |

## Documentation

The [wiki](https://github.com/Darkona/Green-Feathers/wiki) has the details:

- [How It Plays](https://github.com/Darkona/Green-Feathers/wiki/How-It-Plays): every mechanic, with the numbers.
- [Configuration](https://github.com/Darkona/Green-Feathers/wiki/Configuration): every option, and the `/feathers` command.
- [Modpack Makers](https://github.com/Darkona/Green-Feathers/wiki/Modpack-Makers): mounts, armor weights and opt-outs with datapacks.
- [Mod Developers](https://github.com/Darkona/Green-Feathers/wiki/Mod-Developers): the API, to spend feathers from your own mod.

## License

[GNU GPL v3](LICENSE).
