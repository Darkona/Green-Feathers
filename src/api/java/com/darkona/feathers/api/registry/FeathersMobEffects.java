package com.darkona.feathers.api.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The feathers effects. Apply them like any effect: {@code entity.addEffect(new MobEffectInstance(HOT, 200))}.
 */
public final class FeathersMobEffects {

    /** Golden feathers spent before regular ones. */
    public static final DeferredHolder<MobEffect, MobEffect> ENDURANCE = DeferredHolder.create(Registries.MOB_EFFECT, id("endurance"));
    /** Halves regeneration. Applied by cold climates. */
    public static final DeferredHolder<MobEffect, MobEffect> COLD = DeferredHolder.create(Registries.MOB_EFFECT, id("cold"));
    /** Doubles regeneration. */
    public static final DeferredHolder<MobEffect, MobEffect> ENERGIZED = DeferredHolder.create(Registries.MOB_EFFECT, id("energized"));
    /** Doubles costs. Applied by hot climates. */
    public static final DeferredHolder<MobEffect, MobEffect> HOT = DeferredHolder.create(Registries.MOB_EFFECT, id("hot"));
    /** Four fewer max feathers per level. Applied by scorching climates. */
    public static final DeferredHolder<MobEffect, MobEffect> FATIGUE = DeferredHolder.create(Registries.MOB_EFFECT, id("fatigued"));
    /** Halves costs. */
    public static final DeferredHolder<MobEffect, MobEffect> MOMENTUM = DeferredHolder.create(Registries.MOB_EFFECT, id("momentum"));
    /** Shown while Strain is being paid back; slows regeneration. */
    public static final DeferredHolder<MobEffect, MobEffect> STRAINED = DeferredHolder.create(Registries.MOB_EFFECT, id("strain"));
    /** Protects from Heat and Fatigue. */
    public static final DeferredHolder<MobEffect, MobEffect> COOLING = DeferredHolder.create(Registries.MOB_EFFECT, id("cooling"));

    private FeathersMobEffects() {}
}
