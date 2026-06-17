package com.darkona.feathers.api.spi;

import com.darkona.feathers.api.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

/**
 * Implemented by Green Feathers. Other mods should use {@link FeathersAPI}. This internal interface can change
 * between versions.
 *
 * @hidden
 */
@ApiStatus.Internal
public interface FeathersService {

    boolean supports(LivingEntity entity);

    FeathersView view(LivingEntity entity);

    SpendResult spend(LivingEntity entity, ResourceLocation source, int stamina, SpendOptions options);

    int gain(LivingEntity entity, ResourceLocation source, int stamina);

    void setStamina(LivingEntity entity, int stamina);

    void reset(LivingEntity entity);

    SpendResult startDrain(LivingEntity entity, ResourceLocation source, double staminaPerTick, DrainOptions options);

    void stopDrain(LivingEntity entity, ResourceLocation source);

    boolean isDraining(LivingEntity entity, ResourceLocation source);

    void blockRegen(LivingEntity entity, ResourceLocation source, int ticks);

    void unblockRegen(LivingEntity entity, ResourceLocation source);

    void addBonusStamina(LivingEntity entity, ResourceLocation source, int stamina, int ticks);

    void removeBonusStamina(LivingEntity entity, ResourceLocation source);

    void setRestBonus(LivingEntity entity, ResourceLocation source, double multiplier, int ticks);

    void removeRestBonus(LivingEntity entity, ResourceLocation source);

    Climate getClimate(LivingEntity entity);

    int getArmorWeight(LivingEntity entity);

    double getPieceWeight(ItemStack stack);

    void recalculateWeight(LivingEntity entity);

    void registerClimateProvider(ResourceLocation id, int priority, ClimateProvider provider);

    void registerRegenFactor(ResourceLocation id, RegenFactor factor);

    void registerWeightSource(ResourceLocation id, WeightSource source);

    void registerStaminaModifier(ResourceLocation id, int ordinal, StaminaModifier modifier);

    @Nullable Integer dataMapArmorWeight(Item item);

    @Nullable MountStats dataMapMountStats(EntityType<?> type);

    void sync(LivingEntity entity);
}
