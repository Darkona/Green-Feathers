package com.darkona.feathers.climate;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.config.FeathersServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * The fallback climate from biome, weather and sun, at priority 0: any temperature mod's provider outranks it.
 */
public final class VanillaClimate {

    private VanillaClimate() {}

    public static Climate climate(LivingEntity entity) {
        Level level = entity.level();
        BlockPos pos = entity.blockPosition();

        if (entity.isFreezing() || isColdWeather(level, pos)) return Climate.COLD;

        if (entity.wasOnFire || entity.isOnFire() || entity.isInLava()) return Climate.HOT;
        if (level.dimension() == Level.NETHER) return Climate.HOT;
        if (entity.isInPowderSnow || entity.isInWaterOrRain()) return Climate.NEUTRAL;
        if (!level.isDay() || !level.canSeeSky(pos)) return Climate.NEUTRAL;

        return level.getBiome(pos).value().getModifiedClimateSettings().temperature() >= FeathersServerConfig.HOT_TEMPERATURE.get()
                ? Climate.HOT : Climate.NEUTRAL;
    }

    /**
     * Rain or snow falling on a cold biome. Not {@code isRainingAt}: it is false where the precipitation is snow,
     * which is exactly where it's coldest.
     */
    private static boolean isColdWeather(Level level, BlockPos pos) {
        if (!level.isRaining() || !level.canSeeSky(pos)) return false;
        Biome biome = level.getBiome(pos).value();
        if (biome.getPrecipitationAt(pos) == Biome.Precipitation.NONE) return false;
        return biome.coldEnoughToSnow(pos) || biome.getModifiedClimateSettings().temperature() < FeathersServerConfig.COLD_TEMPERATURE.get();
    }
}
