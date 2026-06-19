package com.darkona.feathers.gametest;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.core.Extensions;
import com.darkona.feathers.gametest.scenario.CuriosScenario;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.gametest.TestSupport.assertTrue;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.withCompat;

/**
 * Calls every registered climate provider and regeneration factor on a real player with its compat switched on,
 * so each bridge actually links against the other mod. Meaningful with compat mods on the runtime
 * ({@code -Pcompat=...}). Without them, only the built-in integrations run.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class CompatTests {

    @GameTest(template = "empty")
    public static void everyClimateProviderAnswers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        withCompat(() -> {
            StringBuilder answers = new StringBuilder();
            for (Extensions.ClimateEntry entry : Extensions.climates()) {
                Climate climate = entry.provider().getClimate(player);
                answers.append(entry.id()).append('=').append(climate).append(' ');
            }
            assertTrue(helper, Extensions.climates().length > 0, "no climate providers");
            assertTrue(helper, true, answers.toString());
        });
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyRegenFactorAnswers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        withCompat(() -> {
            for (Extensions.RegenEntry entry : Extensions.regenFactors()) {
                double value = entry.factor().feathersPerSecond(player, FeathersAPI.get(player));
                assertTrue(helper, Double.isFinite(value), entry.id() + " returned " + value);
            }
        });
        helper.succeed();
    }

    /**
     * Curios 5.1 takes slot types by IMC and curios as an item capability: the ring slot exists, and the Feather Ring
     * in it halves armor weight. Passes trivially without Curios.
     */
    @GameTest(template = "empty")
    public static void featherRingIsACurio(GameTestHelper helper) {
        if (ModList.get().isLoaded("curios")) {
            assertTrue(helper, CuriosScenario.hasRingSlot(), "Curios has a ring slot");
            assertValueEqual(helper, CuriosScenario.ringWeightMultiplier(player(helper)), -0.5, "the Feather Ring's modifier in a ring slot");
        }
        helper.succeed();
    }
}
