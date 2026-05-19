package com.darkona.feathers.api;

import net.minecraft.world.entity.LivingEntity;

/**
 * Adds to (or, negative, takes from) an entity's regeneration, in feathers per second, e.g. thirst or diet. All
 * factors are summed into one attribute modifier on {@code greenfeathers:feathers_per_second}. Evaluated about
 * once a second per entity, server side.
 */
@FunctionalInterface
public interface RegenFactor {

    double feathersPerSecond(LivingEntity entity, FeathersView feathers);
}
