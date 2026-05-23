package com.darkona.feathers.api.registry;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The armor weight enchantments.
 */
public final class FeathersEnchantments {

    /** Each level removes a share of an armor piece's weight. */
    public static final RegistryObject<Enchantment> LIGHTWEIGHT = RegistryObject.create(id("lightweight"), ForgeRegistries.ENCHANTMENTS);

    /** Curse: doubles an armor piece's weight. */
    public static final RegistryObject<Enchantment> HEAVY = RegistryObject.create(id("heavy"), ForgeRegistries.ENCHANTMENTS);

    private FeathersEnchantments() {}
}
