package com.darkona.feathers.api.registry;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.MountStats;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Data maps: per-item and per-creature values that mods and datapacks ship as JSON. Each file has
 * {@code "values"}, an object from item or entity type ids (or {@code #tags}) to entries, and an optional
 * {@code "replace": true} that drops what lower packs set. Entries may carry {@code "forge:conditions"}. Loaded with
 * the other server data and synced to clients.
 */
public final class FeathersDataMaps {

    /**
     * Armor weight in feathers per item, for mods and datapacks to ship defaults. Server config rules for the item
     * or its tags take precedence. Path: {@code data/greenfeathers/data_maps/item/armor_weight.json}, e.g.
     * {@code {"values": {"mymod:steel_chestplate": 3}}}.
     */
    public static final ResourceLocation ARMOR_WEIGHT = id("armor_weight");

    /**
     * Per-creature mount tuning; an entry also makes the creature a mount. See {@link MountStats}. Path:
     * {@code data/greenfeathers/data_maps/entity_type/mount_stats.json}.
     */
    public static final ResourceLocation MOUNT_STATS = id("mount_stats");

    private FeathersDataMaps() {}

    /**
     * The item's entry in the armor weight data map, or null without one.
     */
    public static @Nullable Integer armorWeight(Item item) {
        return FeathersAPI.dataMapArmorWeight(item);
    }

    /**
     * The creature's entry in the mount stats data map, or null without one.
     */
    public static @Nullable MountStats mountStats(EntityType<?> type) {
        return FeathersAPI.dataMapMountStats(type);
    }
}
