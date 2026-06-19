package com.darkona.feathers.core;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.config.FeathersServerConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.food.FoodData;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Adds a hunger-based regeneration factor for players. Saturation improves regeneration, while low food reduces it.
 * Mounts are not affected because they do not use player food data.
 */
public final class HungerRegen {

    /** A player at or below this food level is hungry, matching the sprint requirement. */
    static final int HUNGRY_FOOD_LEVEL = 6;

    private HungerRegen() {}

    /** Registers the built-in hunger factor during common setup. */
    public static void registerBuiltIn() {
        Extensions.addRegenFactor(id("hunger"), HungerRegen::regenFactor);
    }

    /**
     * Returns the configured bonus or penalty as a contribution to regeneration.
     */
    private static double regenFactor(LivingEntity entity, FeathersView feathers) {
        if (!(entity instanceof Player player)) return 0.0;

        FoodData food = player.getFoodData();
        int level = food.getFoodLevel();
        double multiplier;
        if (level >= FoodConstants.MAX_FOOD && food.getSaturationLevel() > 0) {
            multiplier = FeathersServerConfig.SATURATION_REGEN_BONUS.get();
        } else if (level <= HUNGRY_FOOD_LEVEL) {
            multiplier = FeathersServerConfig.HUNGER_REGEN_PENALTY.get();
        } else {
            return 0.0;
        }
        if (multiplier == 1.0) return 0.0;

        AttributeInstance regen = player.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
        double base = regen != null ? regen.getBaseValue() : 0.0;
        // A negative base rate drains stamina. Scaling it would make saturation increase the drain.
        return base > 0 ? base * (multiplier - 1.0) : 0.0;
    }
}
