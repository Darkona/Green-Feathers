package com.darkona.feathers.compatibility.coldsweat;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.config.FeathersCompatConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Cold Sweat compatibility, safe to load without Cold Sweat: every call into Cold Sweat goes through
 * {@link ColdSweatBridge}, which only loads when Cold Sweat is present.
 */
public final class ColdSweatCompat {

    public static final boolean LOADED = ModList.get().isLoaded("cold_sweat");

    /** Above the vanilla climate (priority 0): body temperature is the better answer. */
    public static final int CLIMATE_PRIORITY = 100;

    private ColdSweatCompat() {}

    public static boolean isEnabled() {
        return LOADED && FeathersCompatConfig.COLD_SWEAT.get();
    }

    public static void init() {
        if (!LOADED) return;
        FeathersAPI.registerClimateProvider(id("cold_sweat"), CLIMATE_PRIORITY, ColdSweatCompat::climate);
    }

    private static Climate climate(LivingEntity entity) {
        if (!isEnabled()) return null;
        double body = ColdSweatBridge.bodyTemperature(entity);
        if (FeathersCompatConfig.COLD_SWEAT_HEAT.get()) {
            if (body >= FeathersCompatConfig.COLD_SWEAT_SEVERE_THRESHOLD.get()) return Climate.SCORCHING;
            if (body >= FeathersCompatConfig.COLD_SWEAT_HOT_THRESHOLD.get()) return Climate.HOT;
        }
        if (FeathersCompatConfig.COLD_SWEAT_COLD.get() && body <= FeathersCompatConfig.COLD_SWEAT_COLD_THRESHOLD.get()) {
            return Climate.COLD;
        }
        return Climate.NEUTRAL;
    }

    public static boolean canApplyCold(LivingEntity entity) {
        return !isEnabled() || ColdSweatBridge.canApplyCold(entity);
    }

    public static boolean canApplyHeat(LivingEntity entity) {
        return !isEnabled() || ColdSweatBridge.canApplyHeat(entity);
    }
}
