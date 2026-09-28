# Green Feathers

A stamina system for Minecraft 1.21.1 (NeoForge): a bar of **feathers** next to the food bar that any mod can spend.
Based on Elenai's Feathers.

## For players

- **Feathers** regenerate over time (0.4 per second by default). Spending pauses regeneration for a moment.
- **Strain**: when you run out, you can keep going by overspending into red "negative" feathers. Regeneration pays
  them back first, slowly. Resting pays them back faster: stand still, crouch still, or sit (a boat, a mount, a seat).
- **Exhaustion**: with nothing left to spend, you are exhausted until you recover part of the bar.
- **Climate**: cold halves regeneration; heat doubles costs; severe heat (the Nether, fire, lava) also takes 4 max
  feathers (Fatigue). Fire Resistance and the new Potion of Cooling protect you.
- **Armor weight** (off by default): worn armor greys out feathers you can't use. The Lightweight enchantment removes
  25% of a piece's weight per level; the Feather Ring halves the total.
- **Potions**: Endurance (golden bonus feathers), Energy, Momentum, Cooling, Cold, Heat.
- **Basic exertion**: without Actions of Stamina installed, sprinting and jumping cost feathers.
- Commands: `/feathers info|set|reset|max|regen|spend <players> [amount]` (operators).

### The HUD

The feathers sit at the top of the right-hand stack: above food, air bubbles and thirst bars (Thirst Was Taken,
Tough As Nails...), so nothing overlaps. Two feathers per icon, like hearts. Grey feathers are held back by armor
weight, red ones are Strain, a golden row above is bonus feathers (Endurance). Past 20 feathers, further rows are
layered over the first in another color with a row count (or Overflowing Bars' counter). The color shows the
climate: blue when cold, orange when hot. The bar can fade while full; position and offsets are in the client config.

### Configuration

`config/feathers/`:

| File | What |
|---|---|
| `Feathers-Common.toml` | Feathers, regeneration, Strain, exhaustion, effects, resting, armor weights, basic exertion |
| `Feathers-Compat.toml` | One section per supported mod, each with its own `enabled` switch |
| `Feathers-Client.toml` | HUD position, fading, tooltips |

Armor weights are rules, most specific first: `minecraft:iron_chestplate=3` (item), `#mymod:heavy=6` (item tag),
the `greenfeathers:armor_weight` data map, `@minecraft:iron/chestplate=3` (material and piece), `@minecraft:iron=2`
(material).

### Compatibility

Everything turns on by itself when the other mod is installed, and can be turned off in `Feathers-Compat.toml`.
Use one temperature mod and one thirst mod; if several are installed anyway, the climate comes from the first of
Cold Sweat, Tough As Nails, Legendary Survival Overhaul, Serene Seasons, and vanilla biomes.

| Mod | Effect |
|---|---|
| Cold Sweat | Body temperature decides Cold, Heat and Fatigue |
| Thirst Was Taken | Thirst slows regeneration, being quenched speeds it up; regenerating can cost thirst |
| Curios | The Feather Ring goes in a ring slot |
| Overflowing Bars | Row count next to the feathers |
| AppleSkin | Works alongside: the feathers stack above its food overlays; with `regen_uses_hunger`, its exhaustion bar shows what regenerating feathers costs |
| Tough As Nails | Its temperature decides Cold, Heat and Fatigue; its thirst slows or speeds regeneration |
| Legendary Survival Overhaul | The same, from its body temperature and hydration |
| Serene Seasons | Winter outdoors is cold, a summer day in the sun is hot (only without a body-temperature mod) |
| Actions of Stamina | Takes over player actions (sprint, jump, attack, elytra, ParCool, Paragliders...) |

## For mod developers

Compile against the API jar and guard calls with `ModList.get().isLoaded("greenfeathers")`:

```groovy
dependencies {
    compileOnly "com.darkona.feathers:greenfeathers-api:1.21.1-2.0.0"
}
```

Everything goes through `com.darkona.feathers.api.FeathersAPI`. Amounts are **stamina**, a thousandth of a feather
(`Stamina.ofFeathers(2)` is two feathers), so costs can be fractions of a feather. Every call names its source with
your own `ResourceLocation`.

```java
ResourceLocation DASH = ResourceLocation.fromNamespaceAndPath("mymod", "dash");
ResourceLocation GLIDE = ResourceLocation.fromNamespaceAndPath("mymod", "glide");

// A one-off cost, all or nothing. EXEMPT means creative/spectator: let the action happen.
if (FeathersAPI.spend(player, DASH, Stamina.ofFeathers(3)).allowed()) {
    dash(player);
}

// A continuous cost: call every tick while gliding; stop when it's refused.
if (!FeathersAPI.startDrain(player, GLIDE, Stamina.perTick(1.5)).allowed()) {
    stopGliding(player);
}

// Read-only state.
FeathersView feathers = FeathersAPI.get(player);
boolean tired = feathers.exhausted() || feathers.availableFeathers() < 2;
```

More of the API:

- `SpendOptions`: `simulated()`, `withoutStrain()`, `ignoringExhaustion()`, `withRegenDelay(ticks)`.
- `blockRegen` / `unblockRegen`, per source.
- `addBonusStamina`: temporary feathers spent first, like Endurance; saved with the player.
- `setRestBonus`: a hot spring that speeds up Strain recovery.
- Extension points: `registerClimateProvider` (temperature mods), `registerRegenFactor` (thirst, diet...),
  `registerWeightSource` (backpacks), `registerStaminaModifier` (rules that depend on the source).
- Attributes (`FeathersAttributes`): max feathers, max Strain, regeneration, usage multiplier, armor weight
  multiplier. Any item or effect can change them with plain attribute modifiers.
- Effects (`FeathersMobEffects`): Endurance, Cold, Heat, Fatigue, Energized, Momentum, Strained, Cooling.
- Events on `NeoForge.EVENT_BUS`, fired only on changes: `SpendEvent.Pre/Post`, `GainEvent`,
  `DrainEvent.Started/Stopped`, `ExhaustionEvent`, `StrainEvent`, `ArmorWeightEvent`.
- Client: `ClientFeathers.local()` and `predictSpend` for actions decided on the client.

Methods take any `LivingEntity`; today only players have feathers.

## Building

`./gradlew build` produces the mod jar and the `greenfeathers-api` jar. `./gradlew runGameTestServer` runs the tests.

## License

[GNU GPL v3](LICENSE).
