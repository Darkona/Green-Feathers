package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.saveAndLoad;

/**
 * One-off spends: all or nothing, simulation, Strain, exhaustion, bonus stamina, exemptions, multipliers.
 * Defaults: 20 max feathers, 6 max Strain, Strain and exhaustion on.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpendTests {

    private static final ResourceLocation TEST = id("test");

    @GameTest(template = "empty")
    public static void newPlayersStartFull(GameTestHelper helper) {
        FeathersView f = FeathersAPI.get(player(helper));
        assertValueEqual(helper, f.stamina(), Stamina.ofFeathers(20), "stamina");
        assertValueEqual(helper, f.maxFeathers(), 20, "max feathers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendsAreAllOrNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        SpendResult result = FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25), SpendOptions.DEFAULT.withoutStrain());
        assertValueEqual(helper, result, SpendResult.INSUFFICIENT, "spend beyond the bar without Strain");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after a refused spend");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overspendingGoesIntoStrain(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25)), SpendResult.OK, "spend into Strain");
        FeathersView f = FeathersAPI.get(player);
        assertValueEqual(helper, f.stamina(), 0, "stamina");
        assertValueEqual(helper, f.strain(), Stamina.ofFeathers(5), "strain");
        helper.assertFalse(f.exhausted(), "one feather of Strain room is left");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simulatingChangesNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(10)), "can spend 10");
        helper.assertFalse(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(27)), "can't spend past Strain");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after simulating");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendingEverythingExhausts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(26));
        helper.assertTrue(FeathersAPI.get(player).exhausted(), "exhausted with no feathers and no Strain room");
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, 1), SpendResult.EXHAUSTED, "spending while exhausted");
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, 0, SpendOptions.DEFAULT.ignoringExhaustion()), SpendResult.OK, "ignoring exhaustion");

        FeathersAPI.reset(player);
        helper.assertFalse(FeathersAPI.get(player).exhausted(), "reset ends exhaustion");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bonusStaminaIsSpentFirstAndSaved(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.addBonusStamina(player, TEST, 3000, -1);
        FeathersAPI.spend(player, TEST, 2000);

        FeathersView f = FeathersAPI.get(player);
        assertValueEqual(helper, f.bonusStamina(), 1000, "bonus left");
        assertValueEqual(helper, f.stamina(), Stamina.ofFeathers(20), "regular stamina untouched");
        assertValueEqual(helper, saveAndLoad(helper, player).bonusStamina(), 1000, "bonus after a save");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativePlayersAreExempt(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setGameMode(GameType.CREATIVE);
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, Stamina.ofFeathers(5)), SpendResult.EXEMPT, "creative spend");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "creative stamina");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heatDoublesCosts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT.get(), 200));
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3));
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(14), "stamina after a doubled cost");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fractionalCostsWork(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, 250);
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20) - 250, "a quarter feather");
        helper.succeed();
    }
}
