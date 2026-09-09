package com.darkona.feathersoffatigue.api;

import net.minecraft.world.entity.LivingEntity;

/**
 * Adds to an entity's regeneration in feathers per second. Negative values reduce regeneration. All
 * factors are summed into one attribute modifier on {@code feathers_of_fatigue:feathers_per_second}. Evaluated about
 * once a second per entity, server side.
 */
@FunctionalInterface
public interface RegenFactor {

    /**
     * Calculates this source's current contribution to regeneration.
     *
     * @param entity   the entity being updated
     * @param feathers its live stamina view
     * @return the contribution in feathers per second
     */
    double feathersPerSecond(LivingEntity entity, FeathersView feathers);
}
