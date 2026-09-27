package com.darkona.feathersoffatigue.registry;

import com.darkona.feathersoffatigue.api.registry.FeathersIds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.darkona.feathersoffatigue.api.registry.FeathersMobEffects.*;

/**
 * The potions. Their brewing recipes are data, in {@code data/feathers_of_fatigue/recipe/brewing}, written by
 * {@code scripts/brewing/generate.py}.
 */
public final class ModPotions {

    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, FeathersIds.MOD_ID);

    public static final DeferredHolder<Potion, Potion> ENDURANCE_POTION = potion("endurance_potion", ENDURANCE, 2600, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_ENDURANCE_POTION = potion("strong_endurance_potion", ENDURANCE, 2000, 1);
    public static final DeferredHolder<Potion, Potion> LONG_ENDURANCE_POTION = potion("long_endurance_potion", ENDURANCE, 4200, 0);

    public static final DeferredHolder<Potion, Potion> MOMENTUM_POTION = potion("momentum_potion", MOMENTUM, 2600, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_MOMENTUM_POTION = potion("strong_momentum_potion", MOMENTUM, 2000, 1);
    public static final DeferredHolder<Potion, Potion> LONG_MOMENTUM_POTION = potion("long_momentum_potion", MOMENTUM, 4200, 0);

    public static final DeferredHolder<Potion, Potion> COLD_POTION = potion("cold_potion", COLD, 6000, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_COLD_POTION = potion("strong_cold_potion", COLD, 3000, 1);

    public static final DeferredHolder<Potion, Potion> HOT_POTION = potion("hot_potion", HOT, 6000, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_HOT_POTION = potion("strong_hot_potion", HOT, 3000, 1);

    public static final DeferredHolder<Potion, Potion> COOLING_POTION = potion("cooling_potion", COOLING, 3600, 0);
    public static final DeferredHolder<Potion, Potion> LONG_COOLING_POTION = potion("long_cooling_potion", COOLING, 9600, 0);

    public static final DeferredHolder<Potion, Potion> ENERGIZED_POTION = potion("energized_potion", ENERGIZED, 1600, 0);
    public static final DeferredHolder<Potion, Potion> STRONG_ENERGIZED_POTION = potion("strong_energized_potion", ENERGIZED, 1000, 1);
    public static final DeferredHolder<Potion, Potion> LONG_ENERGIZED_POTION = potion("long_energized_potion", ENERGIZED, 2600, 0);

    private static DeferredHolder<Potion, Potion> potion(String name, Holder<MobEffect> effect, int duration, int amplifier) {
        return POTIONS.register(name, () -> new Potion(name, new MobEffectInstance(effect, duration, amplifier)));
    }

    public static void register(IEventBus modEventBus) {
        POTIONS.register(modEventBus);
    }

    private ModPotions() {}
}
