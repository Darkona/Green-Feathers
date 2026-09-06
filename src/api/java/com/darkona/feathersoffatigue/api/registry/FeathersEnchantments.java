package com.darkona.feathersoffatigue.api.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * Data-driven enchantments (see data/feathers_of_fatigue/enchantment).
 */
public final class FeathersEnchantments {

    /** Each level removes a share of an armor piece's weight. */
    public static final ResourceKey<Enchantment> LIGHTWEIGHT = ResourceKey.create(Registries.ENCHANTMENT, id("lightweight"));

    /** Curse: doubles an armor piece's weight. */
    public static final ResourceKey<Enchantment> HEAVY = ResourceKey.create(Registries.ENCHANTMENT, id("heavy"));

    private FeathersEnchantments() {}
}
