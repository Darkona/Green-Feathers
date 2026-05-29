package com.darkona.feathers.api.registry;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The feathers attributes. Change them with ordinary attribute modifiers (items, effects, Curios, commands).
 */
public final class FeathersAttributes {

    /** Maximum feathers. */
    public static final RegistryObject<Attribute> MAX_FEATHERS = RegistryObject.create(id("max_feathers"), ForgeRegistries.ATTRIBUTES);

    /** Maximum Strain, in feathers. */
    public static final RegistryObject<Attribute> MAX_STRAIN = RegistryObject.create(id("max_strain"), ForgeRegistries.ATTRIBUTES);

    /** Regeneration, in feathers per second. */
    public static final RegistryObject<Attribute> FEATHERS_PER_SECOND = RegistryObject.create(id("feathers_per_second"), ForgeRegistries.ATTRIBUTES);

    /** Multiplies every cost. 1.0 = normal. */
    public static final RegistryObject<Attribute> USAGE_MULTIPLIER = RegistryObject.create(id("usage_multiplier"), ForgeRegistries.ATTRIBUTES);

    /** Multiplies armor weight. 1.0 = normal; a MULTIPLY_BASE modifier of -0.5 halves it. */
    public static final RegistryObject<Attribute> ARMOR_WEIGHT_MULTIPLIER = RegistryObject.create(id("armor_weight_multiplier"), ForgeRegistries.ATTRIBUTES);

    private FeathersAttributes() {}
}
