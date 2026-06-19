package com.darkona.feathers.gametest;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.climate.ClimateEffects;
import com.darkona.feathers.compatibility.coldsweat.ColdSweatCompat;
import com.darkona.feathers.compatibility.sereneseasons.SereneSeasonsCompat;
import com.darkona.feathers.compatibility.thirst.ThirstCompat;
import com.darkona.feathers.compatibility.toughasnails.ToughAsNailsCompat;
import com.darkona.feathers.gametest.scenario.ColdSweatScenario;
import com.darkona.feathers.gametest.scenario.SereneSeasonsScenario;
import com.darkona.feathers.gametest.scenario.ThirstScenario;
import com.darkona.feathers.gametest.scenario.ToughAsNailsScenario;
import com.darkona.feathers.config.FeathersCompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.gametest.TestSupport.assertTrue;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.tick;
import static com.darkona.feathers.gametest.TestSupport.withCompat;

/**
 * Drives each survival mod into an extreme with its own API and checks the feathers react: cold, scorching heat,
 * thirst. Each test passes without action when its optional mod is absent ({@code -Pcompat=...}).
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class SurvivalCompatTests {

    /** Applies the climate the providers report and returns it. */
    private static Climate applyClimate(ServerPlayer player) {
        Climate climate = ClimateEffects.evaluate(player);
        ClimateEffects.apply(player, climate);
        return climate;
    }

    private static void assertCold(GameTestHelper helper, ServerPlayer player, String mod) {
        assertValueEqual(helper, applyClimate(player), Climate.COLD, mod + " climate");
        assertTrue(helper, player.hasEffect(FeathersMobEffects.COLD.get()), mod + ": the Cold effect");
    }

    private static void assertScorching(GameTestHelper helper, ServerPlayer player, String mod) {
        assertValueEqual(helper, applyClimate(player), Climate.SCORCHING, mod + " climate");
        assertTrue(helper, player.hasEffect(FeathersMobEffects.HOT.get()), mod + ": the Heat effect");
        assertTrue(helper, player.hasEffect(FeathersMobEffects.FATIGUE.get()), mod + ": the Fatigue effect");
    }

    /** Advances past the 20-tick thirst update interval and reads the resulting regeneration. */
    private static double regenAfterFactors(ServerPlayer player) {
        player.tickCount = 0;
        tick(player, 1);
        return FeathersAPI.getRegenPerSecond(player);
    }

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
            assertTrue(helper, regenAfterFactors(player) < 0.05, "parched: regeneration nearly stops, was " + FeathersAPI.getRegenPerSecond(player));
            ThirstScenario.thirst(player, 20, 10);
            assertTrue(helper, regenAfterFactors(player) > 0.4, "quenched: regeneration above base, was " + FeathersAPI.getRegenPerSecond(player));
        });
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void toughAsNailsTemperatureAndThirst(GameTestHelper helper) {
        if (ToughAsNailsCompat.LOADED) withCompat(() -> {
            ServerPlayer player = player(helper);
            if (ToughAsNailsScenario.temperatureEnabled()) {
                ToughAsNailsScenario.icy(player);
                assertCold(helper, player, "Tough As Nails icy");
                ToughAsNailsScenario.heatstroke(player);
                assertScorching(helper, player, "Tough As Nails hyperthermia");
            }
            if (ToughAsNailsScenario.thirstEnabled()) {
                ToughAsNailsScenario.parched(player);
                assertTrue(helper, regenAfterFactors(player) < 0.05, "parched: regeneration nearly stops, was " + FeathersAPI.getRegenPerSecond(player));
            }
        });
        helper.succeed();
    }

    /**
     * Only meaningful without a body-temperature mod: those outrank the seasons, by design. Forge's GameTest server
     * plays in a normal world, with the tests underground: the player goes to the surface of the nearest biome that
     * can be cold in winter, under open sky.
     */
    @GameTest(template = "empty")
    public static void sereneSeasonsWinterIsCold(GameTestHelper helper) {
        boolean bodyTemperatureMod = ColdSweatCompat.LOADED || ToughAsNailsCompat.LOADED;
        if (!SereneSeasonsCompat.LOADED || bodyTemperatureMod) {
            helper.succeed();
            return;
        }
        ServerPlayer player = player(helper);
        BlockPos column = SereneSeasonsScenario.winterPlace(helper.getLevel(), new BlockPos(helper.absoluteVec(Vec3.ZERO)),
                FeathersCompatConfig.SEASONS_WINTER_MAX_TEMPERATURE.get());
        int surface = helper.getLevel().getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ());
        player.moveTo(column.getX() + 0.5, surface + 2, column.getZ() + 0.5);
        helper.succeedWhen(() -> {
            assertTrue(helper, helper.getLevel().canSeeSky(player.blockPosition()), "no sky light yet at " + player.blockPosition());
            withCompat(() -> winterIsCold(helper, player));
        });
    }

    private static void winterIsCold(GameTestHelper helper, ServerPlayer player) {
        // This Serene Seasons takes the sub-season's enum name (Forge's EnumArgument), and ignores lowercase quietly.
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(), "season set MID_WINTER");
        Climate climate = ClimateEffects.evaluate(player);
        String state = " (season " + SereneSeasonsScenario.season(helper.getLevel()) + ", sky " + helper.getLevel().canSeeSky(player.blockPosition())
                + ", biome temperature " + helper.getLevel().getBiome(player.blockPosition()).value().getModifiedClimateSettings().temperature()
                + ", at " + player.blockPosition() + ")";
        helper.getLevel().getServer().getCommands().performPrefixedCommand(
                helper.getLevel().getServer().createCommandSourceStack().withSuppressedOutput(), "season set MID_SPRING");
        float temperature = helper.getLevel().getBiome(player.blockPosition()).value().getModifiedClimateSettings().temperature();
        boolean coldInWinter = temperature < FeathersCompatConfig.SEASONS_WINTER_MAX_TEMPERATURE.get()
                && !SereneSeasonsScenario.isTropical(helper.getLevel(), player.blockPosition());
        if (coldInWinter) assertValueEqual(helper, climate, Climate.COLD, "outdoors in winter" + state);
        else assertTrue(helper, climate != Climate.COLD || helper.getLevel().isRaining(), "a warm biome isn't cold in winter" + state);
    }
}
