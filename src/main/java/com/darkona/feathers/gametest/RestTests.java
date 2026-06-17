package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.RestState;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.tick;

/**
 * Resting speeds up paying strain back, and so do API rest bonuses.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class RestTests {

    private static ServerPlayer strainedPlayer(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(24), SpendOptions.DEFAULT.withRegenDelay(0));
        return player;
    }

    @GameTest(template = "empty")
    public static void crouchingStillRecoversStrainFaster(GameTestHelper helper) {
        ServerPlayer active = strainedPlayer(helper);
        ServerPlayer resting = strainedPlayer(helper);
        resting.setShiftKeyDown(true);
        resting.setPose(Pose.CROUCHING);

        tick(active, 80);
        tick(resting, 80);

        assertValueEqual(helper, FeathersAPI.get(resting).restState(), RestState.CROUCHING, "rest state");
        int activeStrain = FeathersAPI.get(active).strain();
        int restingStrain = FeathersAPI.get(resting).strain();
        helper.assertTrue(restingStrain < activeStrain, "crouching still should recover faster: " + restingStrain + " vs " + activeStrain);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void restBonusFromApiApplies(GameTestHelper helper) {
        ServerPlayer normal = strainedPlayer(helper);
        ServerPlayer soaking = strainedPlayer(helper);
        FeathersAPI.setRestBonus(soaking, id("test_hot_spring"), 4.0, 100);

        tick(normal, 20);
        tick(soaking, 20);

        assertValueEqual(helper, FeathersAPI.get(soaking).restMultiplier(), 4.0, "rest multiplier");
        int normalPaid = Stamina.ofFeathers(4) - FeathersAPI.get(normal).strain();
        int soakingPaid = Stamina.ofFeathers(4) - FeathersAPI.get(soaking).strain();
        helper.assertTrue(soakingPaid >= 3.5 * normalPaid, "a hot spring pays back about 4x: " + soakingPaid + " vs " + normalPaid);
        helper.succeed();
    }
}
