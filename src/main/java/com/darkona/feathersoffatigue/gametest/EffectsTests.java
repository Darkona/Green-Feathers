package com.darkona.feathersoffatigue.gametest;

import com.darkona.feathersoffatigue.api.Climate;
import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.Stamina;
import com.darkona.feathersoffatigue.api.registry.FeathersIds;
import com.darkona.feathersoffatigue.api.registry.FeathersMobEffects;
import com.darkona.feathersoffatigue.climate.ClimateEffects;
import com.darkona.feathersoffatigue.effect.FeathersMobEffect;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;
import static com.darkona.feathersoffatigue.gametest.TestSupport.assertFalse;
import static com.darkona.feathersoffatigue.gametest.TestSupport.assertTrue;
import static com.darkona.feathersoffatigue.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathersoffatigue.gametest.TestSupport.player;
import static com.darkona.feathersoffatigue.gametest.TestSupport.saveAndLoad;
import static com.darkona.feathersoffatigue.gametest.TestSupport.tick;

/**
 * Heat tiers, heat protection and Endurance.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class EffectsTests {

    @GameTest(template = "empty")
    public static void fireResistanceBlocksHeatEffects(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
        assertFalse(helper, player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT.get(), 200)), "Heat applied despite Fire Resistance");
        assertFalse(helper, player.addEffect(new MobEffectInstance(FeathersMobEffects.FATIGUE.get(), 200)), "Fatigue applied despite Fire Resistance");
        helper.succeed();
    }

    /**
     * Heat that Fire Resistance refuses is not offered to the player again at every climate check.
     */
    @GameTest(template = "empty")
    public static void refusedHeatIsNotOfferedAgain(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600));
        int[] offered = {0};
        Consumer<MobEffectEvent.Applicable> listener = event -> {
            if (event.getEntity() == player && event.getEffectInstance().getEffect() == FeathersMobEffects.HOT.get()) offered[0]++;
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, MobEffectEvent.Applicable.class, listener);
        try {
            ClimateEffects.apply(player, Climate.HOT);
            ClimateEffects.apply(player, Climate.HOT);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        assertFalse(helper, player.hasEffect(FeathersMobEffects.HOT.get()), "Heat under Fire Resistance");
        assertValueEqual(helper, offered[0], 0, "Heat offered under Fire Resistance");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void coolingRemovesActiveHeat(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        assertTrue(helper, player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT.get(), 200)), "Heat was not applied");
        player.addEffect(new MobEffectInstance(FeathersMobEffects.COOLING.get(), 200));
        ClimateEffects.apply(player, Climate.NEUTRAL);
        assertFalse(helper, player.hasEffect(FeathersMobEffects.HOT.get()), "Heat survived Cooling");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void burningIsSevereHeat(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setSecondsOnFire(5);
        Climate climate = ClimateEffects.evaluate(player);
        assertValueEqual(helper, climate, Climate.SCORCHING, "climate while burning");

        ClimateEffects.apply(player, climate);
        tick(player, 10);
        assertTrue(helper, player.hasEffect(FeathersMobEffects.HOT.get()), "burning applies Heat");
        assertTrue(helper, player.hasEffect(FeathersMobEffects.FATIGUE.get()), "burning applies Fatigue");
        assertValueEqual(helper, FeathersAPI.get(player).maxFeathers(), 16, "max feathers under Fatigue");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void leavingHeatLingers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ClimateEffects.apply(player, Climate.SCORCHING);
        ClimateEffects.apply(player, Climate.NEUTRAL);
        MobEffectInstance fatigue = player.getEffect(FeathersMobEffects.FATIGUE.get());
        assertTrue(helper, fatigue != null && !FeathersMobEffect.isPermanent(fatigue), "Fatigue should linger for a while");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void enduranceGivesGoldenFeathersThatSurviveASave(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE.get(), 600));
        FeathersView f = FeathersAPI.get(player);
        assertValueEqual(helper, f.bonusStamina(), Stamina.ofFeathers(8), "Endurance I bonus");

        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(3));
        assertValueEqual(helper, f.bonusStamina(), Stamina.ofFeathers(5), "golden feathers spent first");
        assertValueEqual(helper, f.stamina(), Stamina.ofFeathers(20), "regular feathers untouched");
        assertValueEqual(helper, saveAndLoad(helper, player).bonusStamina(), Stamina.ofFeathers(5), "bonus after a save");

        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(5));
        tick(player, 10);
        assertFalse(helper, player.hasEffect(FeathersMobEffects.ENDURANCE.get()), "Endurance ends when its feathers are spent");
        helper.succeed();
    }

    /**
     * Drinking Endurance again extends what is left of the golden feathers instead of refilling them; a stronger
     * level adds only its extra feathers.
     */
    @GameTest(template = "empty")
    public static void refreshedEnduranceKeepsWhatIsLeft(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE.get(), 600));
        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(3));
        FeathersView f = FeathersAPI.get(player);

        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE.get(), 1200));
        assertValueEqual(helper, f.bonusStamina(), Stamina.ofFeathers(5), "golden feathers after a longer Endurance I");
        assertValueEqual(helper, player.getEffect(FeathersMobEffects.ENDURANCE.get()).getDuration(), 1200, "the longer duration");

        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE.get(), 1200, 1));
        assertValueEqual(helper, f.bonusStamina(), Stamina.ofFeathers(13), "Endurance II adds its eight extra feathers");
        helper.succeed();
    }
}
