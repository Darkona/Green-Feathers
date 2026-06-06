package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.client.FeatherAnimation;
import com.darkona.feathers.api.client.FeatherAnimations;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.config.FeathersClientConfig.StrainAnimation;
import com.darkona.feathers.style.GreenFeatherAnimations;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;

/**
 * The feather animation API, and Green Feathers' triggers with explicit settings (the client config isn't loaded
 * here, so the triggers' own provider is only registered on the client).
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class AnimationTests {

    private static final String MARK = "greenfeathers_animation_test";
    private static final String HIGH_MARK = "greenfeathers_animation_test_high";
    private static final FeatherAnimation SLOW_WAVE = new FeatherAnimation(FeatherAnimation.Kind.WAVE, 0.5f, 3f);

    static {
        FeatherAnimations.registerProvider(id("test_animation_low"), 100, (entity, feathers) -> entity.getTags().contains(MARK) ? SLOW_WAVE : null);
        FeatherAnimations.registerProvider(id("test_animation_high"), 200, (entity, feathers) -> entity.getTags().contains(HIGH_MARK) ? FeatherAnimation.PULSE : null);
    }

    @GameTest(template = "empty")
    public static void higherPriorityAnimationProvidersWin(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        helper.assertTrue(FeatherAnimations.select(player, FeathersAPI.get(player)) == null, "no provider answers");
        player.addTag(MARK);
        assertValueEqual(helper, FeatherAnimations.select(player, FeathersAPI.get(player)), SLOW_WAVE, "the only provider answering");
        player.addTag(HIGH_MARK);
        assertValueEqual(helper, FeatherAnimations.select(player, FeathersAPI.get(player)), FeatherAnimation.PULSE, "the higher priority");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void animationsRefuseNonsense(GameTestHelper helper) {
        boolean refused = false;
        try {
            new FeatherAnimation(FeatherAnimation.Kind.SHAKE, 0f, 1f);
        } catch (IllegalArgumentException e) {
            refused = true;
        }
        helper.assertTrue(refused, "a speed of 0 is refused");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void greenFeathersTriggersPickByState(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        FeathersView full = FeathersAPI.get(player);
        helper.assertTrue(pick(player, full) == null, "full feathers, no effect: still");

        player.addEffect(new MobEffectInstance(FeathersMobEffects.ENERGIZED.get(), 200));
        assertValueEqual(helper, pick(player, FeathersAPI.get(player)), FeatherAnimation.WAVE, "a wave while energized");
        helper.assertTrue(GreenFeatherAnimations.pick(player, FeathersAPI.get(player), StrainAnimation.PULSE, true, 2, false) == null, "the wave can be turned off");

        FeathersAPI.setStamina(player, Stamina.ofFeathers(2));
        assertValueEqual(helper, pick(player, FeathersAPI.get(player)), FeatherAnimation.SHAKE, "two feathers left: a shake, over the wave");
        FeathersAPI.setStamina(player, Stamina.ofFeathers(3));
        assertValueEqual(helper, pick(player, FeathersAPI.get(player)), FeatherAnimation.WAVE, "three left is above the threshold");

        FeathersAPI.setStamina(player, 0);
        FeathersAPI.spend(player, id("test_strain"), Stamina.ofFeathers(2));
        helper.assertTrue(FeathersAPI.get(player).strained(), "strained");
        assertValueEqual(helper, pick(player, FeathersAPI.get(player)), FeatherAnimation.PULSE, "strain first, as set");
        assertValueEqual(helper, GreenFeatherAnimations.pick(player, FeathersAPI.get(player), StrainAnimation.SHAKE, true, 2, true), FeatherAnimation.SHAKE,
                "or a shake");
        assertValueEqual(helper, GreenFeatherAnimations.pick(player, FeathersAPI.get(player), StrainAnimation.NONE, false, 2, true), FeatherAnimation.WAVE,
                "strain and low off: the wave is left");
        helper.succeed();
    }

    private static FeatherAnimation pick(ServerPlayer player, FeathersView feathers) {
        return GreenFeatherAnimations.pick(player, feathers, StrainAnimation.PULSE, true, 2, true);
    }
}
