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
 * The food bar as a regeneration factor, beside the thirst ones: a full bar with saturation left speeds
 * regeneration up, hunger slows it down. Players only; mounts have no food.
 */
public final class HungerRegen {

    /** Food level at or below which a player counts as hungry, as for sprinting. */
    static final int HUNGRY_FOOD_LEVEL = 6;

    private HungerRegen() {}

    public static void registerBuiltIn() {
        Extensions.addRegenFactor(id("hunger"), HungerRegen::regenFactor);
    }

    /**
     * The configured multiplier applied to the base regeneration, as the feathers per second it adds or takes.
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
        // A negative base drains; scaling it would make a full stomach drain faster.
        return base > 0 ? base * (multiplier - 1.0) : 0.0;
    }
}
