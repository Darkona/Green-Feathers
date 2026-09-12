package com.darkona.feathersoffatigue.api;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Tells Feathers of Fatigue how hot or cold an entity is, such as from a temperature mod. Providers run from highest to
 * lowest priority, and the first non-null answer wins. Feathers of Fatigue calls them periodically on the server.
 */
@FunctionalInterface
public interface ClimateProvider {

    /**
     * Evaluates an entity's current climate. Return {@code null} when this provider has no relevant answer.
     *
     * @param entity the entity to evaluate
     * @return the climate, or null to let the next provider decide
     */
    @Nullable
    Climate getClimate(LivingEntity entity);
}
