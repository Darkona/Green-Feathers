package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import net.minecraft.commands.Commands;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.saveAndLoad;

/**
 * One-off spends: all or nothing, simulation, strain, exhaustion, bonus stamina, exemptions, multipliers.
 * Defaults: 20 max feathers, 6 max strain, strain and exhaustion on.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class SpendTests {

    private static final ResourceLocation TEST = id("test");

    @GameTest(template = "empty")
    public static void newPlayersStartFull(GameTestHelper helper) {
        FeathersView f = FeathersAPI.get(player(helper));
        helper.assertValueEqual(f.stamina(), Stamina.ofFeathers(20), "stamina");
        helper.assertValueEqual(f.maxFeathers(), 20, "max feathers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendsAreAllOrNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        SpendResult result = FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25), SpendOptions.DEFAULT.withoutStrain());
        helper.assertValueEqual(result, SpendResult.INSUFFICIENT, "spend beyond the bar without strain");
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after a refused spend");
        helper.succeed();
    }

    /**
     * Huge amounts saturate instead of wrapping into free negative spends. The command rejects values that do not fit.
     */
    @GameTest(template = "empty")
    public static void hugeAmountsSaturate(GameTestHelper helper) {
        helper.assertValueEqual(Stamina.ofFeathers(3_000_000.0), Integer.MAX_VALUE, "huge feathers");
        helper.assertValueEqual(Stamina.ofFeathers(-3_000_000.0), Integer.MIN_VALUE, "huge negative feathers");
        helper.assertValueEqual(Stamina.ofFeathers(Double.POSITIVE_INFINITY), Integer.MAX_VALUE, "infinite feathers");
        helper.assertValueEqual(Stamina.ofFeathers(Double.NaN), 0, "NaN feathers");
        helper.assertValueEqual(Stamina.ofFeathers(3_000_000), Integer.MAX_VALUE, "huge whole feathers");
        helper.assertValueEqual(Stamina.ofFeathers(-3_000_000), Integer.MIN_VALUE, "huge negative whole feathers");
        helper.assertValueEqual(Stamina.toFeathersCeil(Integer.MIN_VALUE), -2_147_483, "ceiling of the lowest stamina");
        helper.assertValueEqual(Stamina.toFeathersCeil(1), 1, "ceiling of a sliver");

        ServerPlayer player = player(helper);
        helper.assertValueEqual(FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3_000_000.0)), SpendResult.INSUFFICIENT, "a huge spend");
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after a huge spend");

        var server = helper.getLevel().getServer();
        var dispatcher = server.getCommands().getDispatcher();
        var source = server.createCommandSourceStack();
        helper.assertTrue(Commands.getParseException(dispatcher.parse("feathers spend @s 3000000", source)) != null, "the command refuses 3000000 feathers");
        helper.assertTrue(Commands.getParseException(dispatcher.parse("feathers set @s 3000000", source)) != null, "and setting them");
        helper.assertTrue(Commands.getParseException(dispatcher.parse("feathers spend @s 2000000", source)) == null, "the command takes 2000000 feathers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overspendingGoesIntoStrain(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertValueEqual(FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25)), SpendResult.OK, "spend into strain");
        FeathersView f = FeathersAPI.get(player);
        helper.assertValueEqual(f.stamina(), 0, "stamina");
        helper.assertValueEqual(f.strain(), Stamina.ofFeathers(5), "strain");
        helper.assertFalse(f.exhausted(), "one feather of strain room is left");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simulatingChangesNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(10)), "can spend 10");
        helper.assertFalse(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(27)), "can't spend past strain");
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after simulating");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendingEverythingExhausts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(26));
        helper.assertTrue(FeathersAPI.get(player).exhausted(), "exhausted with no feathers and no strain room");
        helper.assertValueEqual(FeathersAPI.spend(player, TEST, 1), SpendResult.EXHAUSTED, "spending while exhausted");
        helper.assertValueEqual(FeathersAPI.spend(player, TEST, 0, SpendOptions.DEFAULT.ignoringExhaustion()), SpendResult.OK, "ignoring exhaustion");

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
        helper.assertValueEqual(f.bonusStamina(), 1000, "bonus left");
        helper.assertValueEqual(f.stamina(), Stamina.ofFeathers(20), "regular stamina untouched");
        helper.assertValueEqual(saveAndLoad(helper, player).bonusStamina(), 1000, "bonus after a save");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void creativePlayersAreExempt(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setGameMode(GameType.CREATIVE);
        helper.assertValueEqual(FeathersAPI.spend(player, TEST, Stamina.ofFeathers(5)), SpendResult.EXEMPT, "creative spend");
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "creative stamina");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void heatDoublesCosts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT, 200));
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3));
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(14), "stamina after a doubled cost");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void costsFollowUsageChanges(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(1));
        player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT, 200));
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3));
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(13), "a cost doubled by heat that came after the first spend");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fractionalCostsWork(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, 250);
        helper.assertValueEqual(FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20) - 250, "a quarter feather");
        helper.succeed();
    }
}
