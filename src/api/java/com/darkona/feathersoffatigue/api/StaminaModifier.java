package com.darkona.feathersoffatigue.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/**
 * A global rule on costs and regeneration, applied to every entity that has feathers, in order of its ordinal
 * (lowest first). For plain multipliers prefer attribute modifiers on {@code feathers_of_fatigue:usage_multiplier} or
 * {@code feathers_of_fatigue:feathers_per_second}. Use this interface for rules that need context, such as a cost source.
 * Feathers of Fatigue calls modifiers on the server thread for each spend and regeneration tick. Implementations should
 * avoid allocation in these frequent calls.
 */
public interface StaminaModifier {

    /**
     * Changes a cost before Feathers of Fatigue checks available stamina.
     *
     * @param entity the entity that performs the action
     * @param feathers its live stamina view
     * @param source what the cost is for, as given by the spender
     * @param cost the current cost in stamina units
     * @return the cost to charge, in stamina
     */
    default int modifyCost(LivingEntity entity, FeathersView feathers, Identifier source, int cost) {
        return cost;
    }

    /**
     * Changes the amount generated during one regeneration tick.
     *
     * @param entity the entity that regenerates
     * @param feathers its live stamina view
     * @param regen stamina about to be regenerated this tick
     * @return the stamina to regenerate instead
     */
    default int modifyRegen(LivingEntity entity, FeathersView feathers, int regen) {
        return regen;
    }
}
