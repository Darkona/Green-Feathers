package com.darkona.feathersoffatigue.gametest;

import com.darkona.feathersoffatigue.api.Climate;
import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.Stamina;
import com.darkona.feathersoffatigue.api.registry.FeathersMobEffects;
import com.darkona.feathersoffatigue.climate.ClimateEffects;
import com.darkona.feathersoffatigue.registry.ModPotions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.BrewingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.function.Consumer;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;
import static com.darkona.feathersoffatigue.gametest.TestSupport.player;
import static com.darkona.feathersoffatigue.gametest.TestSupport.saveAndLoad;
import static com.darkona.feathersoffatigue.gametest.TestSupport.tick;

/**
 * Heat tiers, heat protection and Endurance.
 */
public class EffectsTests {

    @GameTest(template = "empty")
    public static void fireResistanceBlocksHeatEffects(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
        helper.assertFalse(player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT, 200)), "Heat applied despite Fire Resistance");
        helper.assertFalse(player.addEffect(new MobEffectInstance(FeathersMobEffects.FATIGUE, 200)), "Fatigue applied despite Fire Resistance");
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
            if (event.getEntity() == player && event.getEffectInstance().is(FeathersMobEffects.HOT)) offered[0]++;
        };
        NeoForge.EVENT_BUS.addListener(MobEffectEvent.Applicable.class, listener);
        try {
            ClimateEffects.apply(player, Climate.HOT);
            ClimateEffects.apply(player, Climate.HOT);
        } finally {
            NeoForge.EVENT_BUS.unregister(listener);
        }
        helper.assertFalse(player.hasEffect(FeathersMobEffects.HOT), "Heat under Fire Resistance");
        helper.assertValueEqual(offered[0], 0, "Heat offered under Fire Resistance");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void coolingRemovesActiveHeat(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT, 200)), "Heat was not applied");
        player.addEffect(new MobEffectInstance(FeathersMobEffects.COOLING, 200));
        ClimateEffects.apply(player, Climate.NEUTRAL);
        helper.assertFalse(player.hasEffect(FeathersMobEffects.HOT), "Heat survived Cooling");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void burningIsSevereHeat(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.igniteForSeconds(5);
        Climate climate = ClimateEffects.evaluate(player);
        helper.assertValueEqual(climate, Climate.SCORCHING, "climate while burning");

        ClimateEffects.apply(player, climate);
        tick(player, 10);
        helper.assertTrue(player.hasEffect(FeathersMobEffects.HOT), "burning applies Heat");
        helper.assertTrue(player.hasEffect(FeathersMobEffects.FATIGUE), "burning applies Fatigue");
        helper.assertValueEqual(FeathersAPI.get(player).maxFeathers(), 16, "max feathers under Fatigue");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void leavingHeatLingers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ClimateEffects.apply(player, Climate.SCORCHING);
        ClimateEffects.apply(player, Climate.NEUTRAL);
        MobEffectInstance fatigue = player.getEffect(FeathersMobEffects.FATIGUE);
        helper.assertTrue(fatigue != null && !fatigue.isInfiniteDuration(), "Fatigue should linger for a while");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void enduranceGivesGoldenFeathersThatSurviveASave(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE, 600));
        FeathersView f = FeathersAPI.get(player);
        helper.assertValueEqual(f.bonusStamina(), Stamina.ofFeathers(8), "Endurance I bonus");

        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(3));
        helper.assertValueEqual(f.bonusStamina(), Stamina.ofFeathers(5), "golden feathers spent first");
        helper.assertValueEqual(f.stamina(), Stamina.ofFeathers(20), "regular feathers untouched");
        helper.assertValueEqual(saveAndLoad(helper, player).bonusStamina(), Stamina.ofFeathers(5), "bonus after a save");

        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(5));
        tick(player, 10);
        helper.assertFalse(player.hasEffect(FeathersMobEffects.ENDURANCE), "Endurance ends when its feathers are spent");
        helper.succeed();
    }

    /**
     * Drinking Endurance again extends what is left of the golden feathers instead of refilling them; a stronger
     * level adds only its extra feathers.
     */
    @GameTest(template = "empty")
    public static void refreshedEnduranceKeepsWhatIsLeft(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE, 600));
        FeathersAPI.spend(player, id("test"), Stamina.ofFeathers(3));
        FeathersView f = FeathersAPI.get(player);

        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE, 1200));
        helper.assertValueEqual(f.bonusStamina(), Stamina.ofFeathers(5), "golden feathers after a longer Endurance I");
        helper.assertValueEqual(player.getEffect(FeathersMobEffects.ENDURANCE).getDuration(), 1200, "the longer duration");

        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENDURANCE, 1200, 1));
        helper.assertValueEqual(f.bonusStamina(), Stamina.ofFeathers(13), "Endurance II adds its eight extra feathers");
        helper.succeed();
    }

    /** The brewing recipes are data: a feather in an awkward splash potion brews a splash Potion of Endurance. */
    @GameTest(template = "empty")
    public static void enduranceBrewsFromData(GameTestHelper helper) {
        BrewingInput input = new BrewingInput(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.AWKWARD), new ItemStack(Items.FEATHER));
        ItemStack brewed = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.BREWING, input, helper.getLevel())
                .map(recipe -> recipe.value().assemble(input)).orElse(ItemStack.EMPTY);
        helper.assertTrue(brewed.is(Items.SPLASH_POTION) && brewed.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(ModPotions.ENDURANCE_POTION),
                "awkward splash potion and a feather brew a splash Potion of Endurance, got " + brewed);
        helper.succeed();
    }
}
