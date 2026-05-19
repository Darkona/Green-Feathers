package com.darkona.feathers.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * A global rule on costs and regeneration, applied to every entity that has feathers, in order of its ordinal
 * (lowest first). For plain multipliers prefer attribute modifiers on {@code greenfeathers:usage_multiplier} or
 * {@code greenfeathers:feathers_per_second}; use this for rules that need context, like the source of a cost.
 * Called on the server thread, on every spend and every tick: don't allocate.
 */
public interface StaminaModifier {

    /**
     * @param source what the cost is for, as given by the spender
     * @return the cost to charge, in stamina
     */
    default int modifyCost(LivingEntity entity, FeathersView feathers, ResourceLocation source, int cost) {
        return cost;
    }

    /**
     * @param regen stamina about to be regenerated this tick
     * @return the stamina to regenerate instead
     */
    default int modifyRegen(LivingEntity entity, FeathersView feathers, int regen) {
        return regen;
    }
}
