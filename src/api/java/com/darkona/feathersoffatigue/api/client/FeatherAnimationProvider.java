package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Animates the feather row by condition. Providers run from highest to lowest priority, and the first non-null answer
 * wins. Without an answer, the row remains still. Built-in triggers use {@link FeatherAnimations#STATUS_PRIORITY}.
 * Feathers of Fatigue calls providers once per client tick, so implementations should avoid expensive work.
 */
@FunctionalInterface
public interface FeatherAnimationProvider {

    /**
     * Selects an animation for one feather row.
     *
     * @param entity   whose feathers the row shows: the local player, or the mount they ride
     * @param feathers those feathers, as the client knows them
     * @return the animation, or null to let the next provider decide
     */
    @Nullable
    FeatherAnimation animationFor(LivingEntity entity, FeathersView feathers);
}
