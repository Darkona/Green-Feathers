package com.darkona.feathersoffatigue.gametest;

// The SurvivalCompatTests of the compats in src/disabled, as they are on Minecraft 1.21.1. Moved back into
// SurvivalCompatTests (with their imports and the scenario classes next to this file) when one of them is enabled
// again; sereneSeasonsWinterIsCold then counts its body-temperature mod again.
final class DisabledSurvivalCompatTests {

    @GameTest(template = "empty")
    public static void coldSweatBodyTemperature(GameTestHelper helper) {
        if (ColdSweatCompat.LOADED) withCompat(() -> {
            ServerPlayer player = player(helper);
            ColdSweatScenario.bodyTemperature(player, -120);
            assertCold(helper, player, "Cold Sweat freezing");
            ColdSweatScenario.bodyTemperature(player, 120);
            assertScorching(helper, player, "Cold Sweat overheating");
        });
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void thirstWasTakenSlowsRegeneration(GameTestHelper helper) {
        if (ThirstCompat.LOADED) withCompat(() -> {
            ServerPlayer player = player(helper);
            ThirstScenario.thirst(player, 0, 0);
            helper.assertTrue(regenAfterFactors(player) < 0.05, "parched: regeneration nearly stops, was " + FeathersAPI.getRegenPerSecond(player));
            ThirstScenario.thirst(player, 20, 10);
            helper.assertTrue(regenAfterFactors(player) > 0.4, "quenched: regeneration above base, was " + FeathersAPI.getRegenPerSecond(player));
        });
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void legendarySurvivalTemperatureAndThirst(GameTestHelper helper) {
        if (LegendarySurvivalCompat.LOADED) withCompat(() -> {
            ServerPlayer player = player(helper);
            LegendarySurvivalScenario.frostbite(player);
            assertCold(helper, player, "LSO frostbite");
            LegendarySurvivalScenario.heatstroke(player);
            assertScorching(helper, player, "LSO heat stroke");
            LegendarySurvivalScenario.parched(player);
            helper.assertTrue(regenAfterFactors(player) < 0.05, "parched: regeneration nearly stops, was " + FeathersAPI.getRegenPerSecond(player));
        });
        helper.succeed();
    }

}
