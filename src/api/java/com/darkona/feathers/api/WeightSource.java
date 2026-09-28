package com.darkona.feathers.api;

import com.darkona.feathers.api.event.ArmorWeightEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * Extra weight, in feathers, on top of worn armor: a backpack, a heavy accessory, a full inventory. Added before
 * {@link ArmorWeightEvent} and the armor weight multiplier. Evaluated whenever the
 * weight is recalculated, server side.
 */
@FunctionalInterface
public interface WeightSource {

    double weight(LivingEntity entity);
}
