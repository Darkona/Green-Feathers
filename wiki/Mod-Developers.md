# Mod Developers

Feathers of Fatigue is built to be spent by other mods. Compile against the API jar and treat it as optional:

```groovy
compileOnly "com.darkona.feathersoffatigue:feathers-of-fatigue-api:26.3-1.0.0-beta.1"
```

Guard every call with `ModList.get().isLoaded("feathers_of_fatigue")`, and keep the calls in a class that only loads when it is: your mod then works with and without Feathers of Fatigue.

`com.darkona.feathersoffatigue.api.FeathersAPI` is the place to start; every class in the API is documented.

## Spending

Costs are in stamina units: a feather is `Stamina.PER_FEATHER` (1000), so costs can be fractions of a feather.

```java
// A one-off cost. EXEMPT (creative players) also means "go ahead".
if (FeathersAPI.spend(player, MY_DASH, Stamina.ofFeathers(3)).allowed()) dash(player);

// A continuous cost: call every tick while it lasts, stop when it's refused.
if (!FeathersAPI.startDrain(player, MY_GLIDE, Stamina.perTick(1.5)).allowed()) stopGliding(player);
```

The source (`MY_DASH`) is your own `Identifier` (`ResourceLocation` on 1.21.1 and older): it shows in `/feathers debug` and in the spend events. `SpendOptions` asks for a spend that never strains, a simulated one, or its own regeneration pause. `canSpend` checks without spending. On the client, a spend predicts the result against the synced feathers, priced like on the server (usage multiplier and stamina modifiers, with the same source); the server's decision is what counts. `ClientFeathers.predictSpend` does the same for actions decided on the client.

## Reading

`FeathersAPI.get(entity)` returns a `FeathersView`: feathers, maximum, strain, bonus, weight, exhaustion, rest state. Players and mounts with feathers have one; everything else answers `hasFeathers() == false`.

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
| `FeatherStyles.registerStyleProvider` | Recolor the player's feathers by condition (client HUD) |
| `FeatherAnimations.registerProvider` | Animate the feather row by condition (client HUD) |

### Weight sources

A weight source can name a color or an item: its share of the weight is then drawn after the boots in that color, or in the item's colors. Without either it's drawn grey.

```java
FeathersAPI.registerWeightSource(MY_BACKPACK, new WeightSource() {
    @Override public double weight(LivingEntity entity) { return backpackWeight(entity); }
    @Override public Item displayItem(LivingEntity entity) { return MyItems.BACKPACK.get(); }
});
// Call FeathersAPI.recalculateWeight(entity) when the backpack's weight changes.
```

## Feather styles

Every feather on the HUD is grayscale sprites tinted with a `FeatherStyle`: a body color and a border color, both ARGB (`0xAARRGGBB`; the alpha multiplies with the HUD's fade, and a border with alpha 0 is not drawn), a variant (the feather's shape), and optionally an overlay drawn over the whole row in two colors of its own. The classes are in `com.darkona.feathersoffatigue.api.client`; none of them touches client classes, so you can register from common setup.

Register a style under your own id, then a `FeatherStyleProvider` that picks it by condition. Providers are asked once per client tick, highest priority first; the first non-null id wins, and with none the player's configured color is used. Feathers of Fatigue's own states (cold, hot, energized, momentum) answer at `FeatherStyles.STATUS_PRIORITY` (0): register above it to win over them, below it to recolor only the plain feathers.

```java
FeatherStyles.registerStyle(SICK, FeatherStyle.opaque(0x7BA05B, 0x2B1D0E).withVariant(FeatherVariants.CRYSTAL));
FeatherStyles.registerStyleProvider(SICK, 100, (player, feathers) -> player.hasEffect(MyEffects.SICKNESS) ? SICK : null);
```

`FeatherVariants` names Feathers of Fatigue's shapes (`FEATHER`, `CRYSTAL`, `GLINT`, `STRAINED`, `PLAIN`) and overlays (`FROST`, `HEAT`). For a shape of your own, draw a row of cells in a texture laid out like Feathers of Fatigue's sheet (see [Resource Packs](Resource-Packs)) and register it with `FeatherVariants.registerVariant(id, texture, row)`, or `registerOverlay` for an overlay. These assume a texture of the sheet's size, `FeatherVariants.SHEET_WIDTH` x `SHEET_HEIGHT` (56x72). For a texture of another size, pass it: `registerVariant(id, texture, row, width, height)`. Both sizes must be multiples of 8 (pad with transparent pixels), the texture must be wide enough for the row's cells (six for a variant, two for an overlay), and the row must fit in its height; otherwise registration throws `IllegalArgumentException`.

Feathers of Fatigue registers its own styles the same way, under the ids in `FeatherStyles` (`GREEN`, `COLD`, `STRAIN`, `ENDURANCE`, `ARMOR`, `EMPTY`, `EXHAUSTED`...): registering one of those ids replaces it. Resource packs' `feather_styles.json` wins over what code registers, so players and packs keep the last word. `FeatherStyle.CODEC` reads and writes the same JSON form, for mods with configs of their own.

Mount rows, armor pieces and colored weight sources keep the colors of their textures and items; a style provider only picks the player's own feathers.

## Feather animations

The row can move like the vanilla hearts: `WAVE` (a bump running along it, like Regeneration), `SHAKE` (random jitter, like low health) or `PULSE` (the feathers brighten and dim). A `FeatherAnimation` is a kind, a speed (1 is the default pace) and an amplitude (pixels, or for `PULSE` how far toward white, 0 to 1).

A `FeatherAnimationProvider` picks one by condition, for the entity whose feathers the row shows: the local player, or the mount they ride. Providers are asked once per client tick, highest priority first, and the first non-null answer wins. Feathers of Fatigue's own triggers answer at `FeatherAnimations.STATUS_PRIORITY` (0), in this order: strain, few feathers left, Energized. Players set each of them in the client config.

```java
FeatherAnimations.registerProvider(MY_FEVER, 50, (entity, feathers) -> entity.hasEffect(MyEffects.FEVER) ? FeatherAnimation.SHAKE : null);
```

## Events

On the NeoForge event bus: `SpendEvent.Pre` (cancel or change a spend) and `SpendEvent.Post`, `GainEvent`, `DrainEvent`, `ExhaustionEvent`, `StrainEvent`, `RegenEvent`, and `ArmorWeightEvent` (change the total weight).

## Older Minecraft versions

The API (`feathers-of-fatigue-api`) is the same in every version, at `<minecraft>-1.0.0` (`1.21.1-1.0.0`, `1.20.1-1.0.0`...). On 1.21.1 and older, ids are Minecraft's `ResourceLocation` where 26.x and 1.21.11 have `Identifier`. Forge needs a few more changes on 1.20.1 and older:

- Attributes, effects and enchantments are `RegistryObject`s (`FeathersAttributes.MAX_FEATHERS.get()`), not `DeferredHolder`s.
- Events go on `MinecraftForge.EVENT_BUS`. `SpendEvent.Post#getSpendResult` replaces `getResult`, which Forge's `Event` already has.
- `FeathersDataMaps` holds the data map ids and reads them: `FeathersDataMaps.armorWeight(item)`, `FeathersDataMaps.mountStats(type)`. Data maps are loaded by Feathers of Fatigue itself, from the same paths and format.
- Attribute modifier operations have their old names: a Feather Ring is a `MULTIPLY_BASE` modifier of -0.5 on `feathers_of_fatigue:armor_weight_multiplier`.
- **1.18.2 only:** events are Forge 40 `LivingEvent`s; `getEntityLiving()` gives the entity.
