# Disabled compats

Compats with mods that have no NeoForge build for this Minecraft version: Cold Sweat, Thirst Was Taken and Legendary Survival Overhaul (none for 26.x), and Tough As Nails (none for 26.3). Their code is kept here as it is for Minecraft 1.21.1, and for Tough As Nails as on 26.2, so it is not lost.

Nothing under `src/disabled` is compiled or packaged: it is not a Gradle source set. The parts that lived in shared files are here too:

- `java/.../config/DisabledCompatConfig.java`: their fields and sections of `FeathersCompatConfig`.
- `java/.../gametest/DisabledSurvivalCompatTests.java`: their tests from `SurvivalCompatTests`, with the scenario classes in `gametest/scenario`.
- `resources/META-INF/neoforge.mods.toml.disabled`: their optional dependencies.
- Cold Sweat also vetoes Cold and Heat: `ColdSweatCompat.canApplyCold` and `canApplyHeat`, called from the `canApply` of the cold and hot effects in `ModEffects`.

Enabling one again is a port of its own: move its files back under `src/main`, update it to the mod's API for this Minecraft version, add its dependency to `build.gradle`, `gradle.properties` and `neoforge.mods.toml`, its call to `Feathers`, its switch to `TestSupport.COMPATS`, and run its game tests with `-Pcompat=...`.
