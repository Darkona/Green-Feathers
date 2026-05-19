package com.darkona.feathers.api.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class FeathersIds {

    public static final String MOD_ID = "greenfeathers";

    private FeathersIds() {}

    /**
     * Entity types that count as mounts besides every {@code AbstractHorse}: add your rideable creatures here
     * ({@code data/greenfeathers/tags/entity_types/mounts.json}) and they get feathers while mounts are enabled. An
     * entry in the {@code greenfeathers:mount_stats} data map does the same and tunes them too.
     */
    public static final TagKey<EntityType<?>> MOUNTS = TagKey.create(Registries.ENTITY_TYPE, id("mounts"));

    /**
     * Opt-out: entity types here never get feathers, even horses. For special mounts that shouldn't tire.
     */
    public static final TagKey<EntityType<?>> NO_FEATHERS = TagKey.create(Registries.ENTITY_TYPE, id("no_feathers"));

    @SuppressWarnings("removal") // The constructor is the only way on every Forge 47 build.
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
