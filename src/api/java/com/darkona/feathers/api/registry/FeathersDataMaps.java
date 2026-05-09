package com.darkona.feathers.api.registry;

import com.darkona.feathers.api.MountStats;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import static com.darkona.feathers.api.registry.FeathersIds.id;

public final class FeathersDataMaps {

    /**
     * Armor weight in feathers per item, for mods and datapacks to ship defaults. Server config rules for the item
     * or its tags take precedence. Path: {@code data/<namespace>/data_maps/item/armor_weight.json}, e.g.
     * {@code {"values": {"mymod:steel_chestplate": 3}}}.
     */
    public static final DataMapType<Item, Integer> ARMOR_WEIGHT = DataMapType
            .builder(id("armor_weight"), Registries.ITEM, Codec.intRange(0, 1000))
            .synced(Codec.INT, false)
            .build();

    /**
     * Per-creature mount tuning; an entry also makes the creature a mount. See {@link MountStats}.
     */
    public static final DataMapType<EntityType<?>, MountStats> MOUNT_STATS = DataMapType
            .builder(id("mount_stats"), Registries.ENTITY_TYPE, MountStats.CODEC)
            // Synced: the client decides whether a mount has feathers (HUD, jump prediction) from it too.
            .synced(MountStats.CODEC, false)
            .build();

    private FeathersDataMaps() {}
}
