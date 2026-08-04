# Mod Developers

Green Feathers is built to be spent by other mods. Compile against the API jar and treat it as optional:

```groovy
compileOnly "com.darkona.feathers:greenfeathers-api:1.21.1-2.0.0"
```

Guard every call with `ModList.get().isLoaded("greenfeathers")`, and keep the calls in a class that only loads when
it is: your mod then works with and without Green Feathers.

`com.darkona.feathers.api.FeathersAPI` is the place to start; every class in the API is documented.

## Spending

Costs are in stamina units: a feather is `Stamina.PER_FEATHER` (1000), so costs can be fractions of a feather.

```java
// A one-off cost. EXEMPT (creative players) also means "go ahead".
if (FeathersAPI.spend(player, MY_DASH, Stamina.ofFeathers(3)).allowed()) dash(player);

// A continuous cost: call every tick while it lasts, stop when it's refused.
if (!FeathersAPI.startDrain(player, MY_GLIDE, Stamina.perTick(1.5)).allowed()) stopGliding(player);
```

The source (`MY_DASH`) is your own `ResourceLocation`: it shows in `/feathers debug` and in the spend events. `SpendOptions` asks for a spend that never strains, a simulated one, or its own regeneration pause. `canSpend` checks without spending. On the client, a spend predicts the result against the synced feathers, priced like on the server (usage multiplier and stamina modifiers, with the same source); the server's decision is what counts. `ClientFeathers.predictSpend` does the same for actions decided on the client.

## Reading

`FeathersAPI.get(entity)` returns a `FeathersView`: feathers, maximum, strain, bonus, weight, exhaustion, rest state.
Players and mounts with feathers have one; everything else answers `hasFeathers() == false`.

## Hooks

| Hook | For |
|---|---|
| `registerClimateProvider` | Temperature mods: decide when an entity is cold, hot, or overheating |
| `registerRegenFactor` | Thirst and similar: speed regeneration up or down |
| `registerStaminaModifier` | Change what a spend costs |
| `registerWeightSource` | Add weight of your own (a backpack, a full inventory) |
| `setRestBonus` / `removeRestBonus` | A hot spring or a bench that helps recovery |
| `addBonusStamina` / `removeBonusStamina` | Temporary bonus feathers |
| `blockRegen` / `unblockRegen` | Pause regeneration |

### Weight sources

A weight source can name a color or an item: its share of the weight is then drawn after the boots in that color,
or in the item's colors. Without either it's drawn grey.

```java
FeathersAPI.registerWeightSource(MY_BACKPACK, new WeightSource() {
    @Override public double weight(LivingEntity entity) { return backpackWeight(entity); }
    @Override public Item displayItem(LivingEntity entity) { return MyItems.BACKPACK.get(); }
});
// Call FeathersAPI.recalculateWeight(entity) when the backpack's weight changes.
```

## Events

On the NeoForge event bus: `SpendEvent.Pre` (cancel or change a spend) and `SpendEvent.Post`, `GainEvent`,
`DrainEvent`, `ExhaustionEvent`, `StrainEvent`, `RegenEvent`, and `ArmorWeightEvent` (change the total weight).
