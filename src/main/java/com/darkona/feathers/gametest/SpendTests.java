package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.event.StrainEvent;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersServiceImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.saveAndLoad;
import static com.darkona.feathers.gametest.TestSupport.tick;

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
        assertValueEqual(helper, f.stamina(), Stamina.ofFeathers(20), "stamina");
        assertValueEqual(helper, f.maxFeathers(), 20, "max feathers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendsAreAllOrNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        SpendResult result = FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25), SpendOptions.DEFAULT.withoutStrain());
        assertValueEqual(helper, result, SpendResult.INSUFFICIENT, "spend beyond the bar without strain");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after a refused spend");
        helper.succeed();
    }

    /**
     * Huge amounts saturate instead of wrapping into free negative spends. The command rejects values that do not fit.
     */
    @GameTest(template = "empty")
    public static void hugeAmountsSaturate(GameTestHelper helper) {
        assertValueEqual(helper, Stamina.ofFeathers(3_000_000.0), Integer.MAX_VALUE, "huge feathers");
        assertValueEqual(helper, Stamina.ofFeathers(-3_000_000.0), Integer.MIN_VALUE, "huge negative feathers");
        assertValueEqual(helper, Stamina.ofFeathers(Double.POSITIVE_INFINITY), Integer.MAX_VALUE, "infinite feathers");
        assertValueEqual(helper, Stamina.ofFeathers(Double.NaN), 0, "NaN feathers");
        assertValueEqual(helper, Stamina.ofFeathers(3_000_000), Integer.MAX_VALUE, "huge whole feathers");
        assertValueEqual(helper, Stamina.ofFeathers(-3_000_000), Integer.MIN_VALUE, "huge negative whole feathers");
        assertValueEqual(helper, Stamina.toFeathersCeil(Integer.MIN_VALUE), -2_147_483, "ceiling of the lowest stamina");
        assertValueEqual(helper, Stamina.toFeathersCeil(1), 1, "ceiling of a sliver");

        ServerPlayer player = player(helper);
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3_000_000.0)), SpendResult.INSUFFICIENT, "a huge spend");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after a huge spend");

        MinecraftServer server = helper.getLevel().getServer();
        CommandSourceStack source = server.createCommandSourceStack();
        // A parse that stops before the end of the input refused an argument.
        helper.assertTrue(server.getCommands().getDispatcher().parse("feathers spend @s 3000000", source).getReader().canRead(), "the command refuses 3000000 feathers");
        helper.assertTrue(server.getCommands().getDispatcher().parse("feathers set @s 3000000", source).getReader().canRead(), "and setting them");
        helper.assertFalse(server.getCommands().getDispatcher().parse("feathers spend @s 2000000", source).getReader().canRead(), "the command takes 2000000 feathers");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void overspendingGoesIntoStrain(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        assertValueEqual(helper, FeathersAPI.spend(player, TEST, Stamina.ofFeathers(25)), SpendResult.OK, "spend into strain");
        FeathersView f = FeathersAPI.get(player);
        assertValueEqual(helper, f.stamina(), 0, "stamina");
        assertValueEqual(helper, f.strain(), Stamina.ofFeathers(5), "strain");
        helper.assertFalse(f.exhausted(), "one feather of strain room is left");
        helper.succeed();
    }

    /**
     * A gift while strained pays the strain back first, as regeneration does: stamina and strain never sit side by
     * side. Clearing the last of it posts the event, like the tick's own recovery.
     */
    @GameTest(template = "empty")
    public static void gainsPayStrainBackFirst(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(24));
        boolean[] cleared = {false};
        Consumer<StrainEvent.Cleared> listener = event -> cleared[0] |= event.getEntity() == player;
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, StrainEvent.Cleared.class, listener);
        try {
            assertValueEqual(helper, FeathersAPI.gain(player, TEST, Stamina.ofFeathers(3)), Stamina.ofFeathers(3), "stamina used by a gift under strain");
            FeathersView f = FeathersAPI.get(player);
            assertValueEqual(helper, f.strain(), Stamina.ofFeathers(1), "strain left after the gift");
            assertValueEqual(helper, f.stamina(), 0, "no stamina while strain is left");
            helper.assertFalse(cleared[0], "strain not cleared yet");

            assertValueEqual(helper, FeathersAPI.gain(player, TEST, Stamina.ofFeathers(5)), Stamina.ofFeathers(5), "stamina used by a gift past the strain");
            assertValueEqual(helper, f.strain(), 0, "strain paid back");
            assertValueEqual(helper, f.stamina(), Stamina.ofFeathers(4), "the rest of the gift");
            helper.assertTrue(cleared[0], "clearing the strain posts its event");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    /**
     * Strain turned off in the config while a player is strained: the strain goes on the next tick, with its event,
     * and the strained effect is not put on the player on the way out.
     */
    @GameTest(template = "empty")
    public static void strainTurnedOffClearsStrainWithItsEvent(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(24));
        boolean[] cleared = {false};
        Consumer<StrainEvent.Cleared> listener = event -> cleared[0] |= event.getEntity() == player;
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, StrainEvent.Cleared.class, listener);
        boolean strain = FeathersServerConfig.ENABLE_STRAIN.get();
        try {
            FeathersServerConfig.ENABLE_STRAIN.set(false);
            tick(player, 1);
            assertValueEqual(helper, FeathersAPI.get(player).strain(), 0, "strain with strain turned off");
            helper.assertTrue(cleared[0], "dropping the strain posts its event");
            helper.assertFalse(player.hasEffect(FeathersMobEffects.STRAINED.get()), "no strained effect without strain");
        } finally {
            FeathersServerConfig.ENABLE_STRAIN.set(strain);
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void simulatingChangesNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(10)), "can spend 10");
        helper.assertFalse(FeathersAPI.canSpend(player, TEST, Stamina.ofFeathers(27)), "can't spend past strain");
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(20), "stamina after simulating");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void spendingEverythingExhausts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(26));
        helper.assertTrue(FeathersAPI.get(player).exhausted(), "exhausted with no feathers and no strain room");
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
    public static void costsFollowUsageChanges(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(1));
        player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT.get(), 200));
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3));
        assertValueEqual(helper, FeathersAPI.get(player).stamina(), Stamina.ofFeathers(13), "a cost doubled by heat that came after the first spend");
        helper.succeed();
    }

    /**
     * The client prices a predicted spend like the server: the usage multiplier (Heat doubles it) and the stamina
     * modifiers, against the view it has.
     */
    @GameTest(template = "empty")
    public static void clientPricingMatchesTheServer(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(FeathersMobEffects.HOT.get(), 200));
        int priced = FeathersServiceImpl.effectiveCost(player, view(false), player.getAttribute(FeathersAttributes.USAGE_MULTIPLIER.get()), TEST, Stamina.ofFeathers(3));
        FeathersAPI.spend(player, TEST, Stamina.ofFeathers(3));
        assertValueEqual(helper, priced, Stamina.ofFeathers(20) - FeathersAPI.get(player).stamina(), "the client's price against the server's");
        assertValueEqual(helper, priced, Stamina.ofFeathers(6), "a cost doubled by heat");
        helper.succeed();
    }

    /** A view as the client or a mount's rider sees it: 3 feathers, 2 of strain room out of 4, exhausted or not. */
    private static FeathersView view(boolean exhausted) {
        return new FeathersView() {
            public boolean hasFeathers() { return true; }
            public int stamina() { return Stamina.ofFeathers(3); }
            public int maxStamina() { return Stamina.ofFeathers(20); }
            public int availableStamina() { return Stamina.ofFeathers(3); }
            public int strain() { return Stamina.ofFeathers(2); }
            public int maxStrain() { return Stamina.ofFeathers(4); }
            public boolean exhausted() { return exhausted; }
        };
    }

    @GameTest(template = "empty")
    public static void viewChecksFollowTheOptions(GameTestHelper helper) {
        boolean strain = FeathersServerConfig.ENABLE_STRAIN.get();
        try {
            FeathersServerConfig.ENABLE_STRAIN.set(true);
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(false), Stamina.ofFeathers(5), true, false), SpendResult.OK, "stamina plus strain room");
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(false), Stamina.ofFeathers(6), true, false), SpendResult.INSUFFICIENT, "past the strain room");
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(false), Stamina.ofFeathers(4), false, false), SpendResult.INSUFFICIENT, "no strain for this spend");
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(true), Stamina.ofFeathers(1), true, false), SpendResult.EXHAUSTED, "exhausted");
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(true), Stamina.ofFeathers(1), true, true), SpendResult.OK, "exhaustion ignored");
            FeathersServerConfig.ENABLE_STRAIN.set(false);
            assertValueEqual(helper, FeathersServiceImpl.simulateAgainst(view(false), Stamina.ofFeathers(4), true, false), SpendResult.INSUFFICIENT, "strain off on the server");
        } finally {
            FeathersServerConfig.ENABLE_STRAIN.set(strain);
        }
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
