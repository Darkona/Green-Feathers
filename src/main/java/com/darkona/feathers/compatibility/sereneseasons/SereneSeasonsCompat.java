package com.darkona.feathers.compatibility.sereneseasons;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.config.FeathersCompatConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Serene Seasons compatibility, safe to load without it: calls go through {@link SereneSeasonsBridge}. Winter
 * outdoors is cold; a summer day in the sun is hot in warmer biomes. Below the body-temperature mods (Cold Sweat,
 * Tough As Nails), which already account for seasons; above the vanilla climate.
 * Anything it has no opinion on falls through to the vanilla climate.
 */
public final class SereneSeasonsCompat {

    public static final boolean LOADED = ModList.get().isLoaded("sereneseasons");

    public static final int CLIMATE_PRIORITY = 50;

    private SereneSeasonsCompat() {}

    public static void init() {
        if (LOADED) FeathersAPI.registerClimateProvider(id("serene_seasons"), CLIMATE_PRIORITY, SereneSeasonsCompat::climate);
    }

    private static Climate climate(LivingEntity entity) {
        if (!FeathersCompatConfig.SEASONS.get()) return null;
        Level level = entity.level;
        BlockPos pos = entity.blockPosition();
        if (!level.canSeeSky(pos) || SereneSeasonsBridge.isTropical(level, pos)) return null;

        return switch (SereneSeasonsBridge.season(level)) {
            case WINTER -> FeathersCompatConfig.SEASONS_WINTER_COLD.get()
                    && level.getBiome(pos).value().getBaseTemperature() < FeathersCompatConfig.SEASONS_WINTER_MAX_TEMPERATURE.get()
                    ? Climate.COLD : null;
            case SUMMER -> FeathersCompatConfig.SEASONS_SUMMER_HEAT.get() && level.isDay() && !entity.isInWaterOrRain()
                    && level.getBiome(pos).value().getBaseTemperature() >= FeathersCompatConfig.SEASONS_SUMMER_MIN_TEMPERATURE.get()
                    ? Climate.HOT : null;
            case SPRING, AUTUMN -> null;
        };
    }
}
