package com.darkona.feathers.api;

import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Tells Green Feathers how hot or cold an entity is, e.g. from a temperature mod. Providers are asked in order of
 * priority, highest first; the first non-null answer wins. Called every couple of seconds per entity, server side.
 */
@FunctionalInterface
public interface ClimateProvider {

    /**
     * @return the climate, or null to let the next provider decide
     */
    @Nullable
    Climate getClimate(LivingEntity entity);
}
