package com.darkona.feathersoffatigue.style;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.Stamina;
import com.darkona.feathersoffatigue.api.client.FeatherAnimation;
import com.darkona.feathersoffatigue.api.client.FeatherAnimations;
import com.darkona.feathersoffatigue.config.FeathersClientConfig;
import com.darkona.feathersoffatigue.config.FeathersClientConfig.StrainAnimation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * Feathers of Fatigue's own animation triggers, registered like any other mod's: strain first, then few feathers left, then
 * Energized. Endurance and Momentum do not animate because their rows already use distinct visual styles.
 */
public final class BuiltInFeatherAnimations {

    public static final ResourceLocation STATUS_PROVIDER = id("status");

    private BuiltInFeatherAnimations() {}

    /** From the client config: call on the client only. */
    public static void register() {
        FeatherAnimations.registerProvider(STATUS_PROVIDER, FeatherAnimations.STATUS_PRIORITY, (entity, feathers) -> pick(entity, feathers,
                FeathersClientConfig.STRAIN_ANIMATION.get(), FeathersClientConfig.SHAKE_WHEN_LOW.get(), FeathersClientConfig.LOW_FEATHERS.get(),
                FeathersClientConfig.WAVE_WHEN_ENERGIZED.get()));
    }

    /**
     * The trigger that applies, by the given settings: strain, then {@code lowFeathers} or fewer usable feathers (when
     * the maximum is above that), then Energized.
     */
    public static @Nullable FeatherAnimation pick(LivingEntity entity, FeathersView feathers, StrainAnimation strain, boolean shakeWhenLow,
                                                  int lowFeathers, boolean waveWhenEnergized) {
        if (feathers.strained() && strain != StrainAnimation.NONE) return strain == StrainAnimation.SHAKE ? FeatherAnimation.SHAKE : FeatherAnimation.PULSE;
        if (shakeWhenLow && feathers.maxFeathers() > lowFeathers && Stamina.toFeathers(feathers.availableStamina()) <= lowFeathers) {
            return FeatherAnimation.SHAKE;
        }
        if (waveWhenEnergized && FeathersAPI.isEnergized(entity)) return FeatherAnimation.WAVE;
        return null;
    }
}
