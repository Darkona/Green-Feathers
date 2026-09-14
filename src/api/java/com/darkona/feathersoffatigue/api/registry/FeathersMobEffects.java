package com.darkona.feathersoffatigue.api.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * The feathers effects. Apply them like any effect: {@code entity.addEffect(new MobEffectInstance(HOT.get(), 200))}.
 */
public final class FeathersMobEffects {

    /** Golden feathers spent before regular ones. */
    public static final RegistryObject<MobEffect> ENDURANCE = RegistryObject.create(id("endurance"), ForgeRegistries.MOB_EFFECTS);
    /** Halves regeneration. Applied by cold climates. */
    public static final RegistryObject<MobEffect> COLD = RegistryObject.create(id("cold"), ForgeRegistries.MOB_EFFECTS);
    /** Doubles regeneration. */
    public static final RegistryObject<MobEffect> ENERGIZED = RegistryObject.create(id("energized"), ForgeRegistries.MOB_EFFECTS);
    /** Doubles costs. Applied by hot climates. */
    public static final RegistryObject<MobEffect> HOT = RegistryObject.create(id("hot"), ForgeRegistries.MOB_EFFECTS);
    /** Four fewer max feathers per level. Applied by scorching climates. */
    public static final RegistryObject<MobEffect> FATIGUE = RegistryObject.create(id("fatigued"), ForgeRegistries.MOB_EFFECTS);
    /** Halves costs. */
    public static final RegistryObject<MobEffect> MOMENTUM = RegistryObject.create(id("momentum"), ForgeRegistries.MOB_EFFECTS);
    /** Appears while the entity recovers from strain and slows regeneration. */
    public static final RegistryObject<MobEffect> STRAINED = RegistryObject.create(id("strain"), ForgeRegistries.MOB_EFFECTS);
    /** Protects from the Hot and Fatigue effects. */
    public static final RegistryObject<MobEffect> COOLING = RegistryObject.create(id("cooling"), ForgeRegistries.MOB_EFFECTS);

    private FeathersMobEffects() {}
}
