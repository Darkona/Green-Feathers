package com.darkona.feathers.effect;

import com.darkona.feathers.api.registry.FeathersMobEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Base of the feathers effects. {@link #canApply} is binding: {@link EffectEvents} refuses the effect otherwise.
 */
public class FeathersMobEffect extends MobEffect {

    public FeathersMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * Whether the entity may have this effect now. Creative and invulnerable players take none.
     */
    public boolean canApply(LivingEntity entity) {
        return !(entity instanceof Player player && (player.isCreative() || player.getAbilities().invulnerable));
    }

    /**
     * The effect was added to an entity that {@link #canApply} accepted.
     */
    public void onApplied(LivingEntity entity, MobEffectInstance instance) {}

    /**
     * The effect was removed or expired.
     */
    public void onEnded(LivingEntity entity, MobEffectInstance instance) {}

    /**
     * Fire Resistance and Cooling protect from every heat effect.
     */
    public static boolean isProtectedFromHeat(LivingEntity entity) {
        return entity.hasEffect(MobEffects.FIRE_RESISTANCE) || entity.hasEffect(FeathersMobEffects.COOLING);
    }

}
