package com.darkona.feathersoffatigue.api.spi;

import com.darkona.feathersoffatigue.api.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implemented by Feathers of Fatigue. Other mods should use {@link FeathersAPI}. This internal interface can change
 * between versions.
 *
 * @hidden
 */
@ApiStatus.Internal
public interface FeathersService {

    boolean supports(LivingEntity entity);

    FeathersView view(LivingEntity entity);

    SpendResult spend(LivingEntity entity, Identifier source, int stamina, SpendOptions options);

    int gain(LivingEntity entity, Identifier source, int stamina);

    void setStamina(LivingEntity entity, int stamina);

    void reset(LivingEntity entity);

    SpendResult startDrain(LivingEntity entity, Identifier source, double staminaPerTick, DrainOptions options);

    void stopDrain(LivingEntity entity, Identifier source);

    boolean isDraining(LivingEntity entity, Identifier source);

    void blockRegen(LivingEntity entity, Identifier source, int ticks);

    void unblockRegen(LivingEntity entity, Identifier source);

    void addBonusStamina(LivingEntity entity, Identifier source, int stamina, int ticks);

    void removeBonusStamina(LivingEntity entity, Identifier source);

    void setRestBonus(LivingEntity entity, Identifier source, double multiplier, int ticks);

    void removeRestBonus(LivingEntity entity, Identifier source);

    Climate getClimate(LivingEntity entity);

    int getArmorWeight(LivingEntity entity);

    double getPieceWeight(ItemStack stack);

    void recalculateWeight(LivingEntity entity);

    void registerClimateProvider(Identifier id, int priority, ClimateProvider provider);

    void registerRegenFactor(Identifier id, RegenFactor factor);

    void registerWeightSource(Identifier id, WeightSource source);

    void registerStaminaModifier(Identifier id, int ordinal, StaminaModifier modifier);

    void sync(LivingEntity entity);
}
