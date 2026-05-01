package com.darkona.feathers.compatibility.sereneseasons;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import sereneseasons.api.season.SeasonHelper;

/**
 * The only class that touches Serene Seasons. Loaded only through {@link SereneSeasonsCompat} when it is present.
 */
final class SereneSeasonsBridge {

    /** Serene Seasons' seasons, mirrored so the compat class never names its enum. */
    enum Season {SPRING, SUMMER, AUTUMN, WINTER}

    private static final Season[] SEASONS = Season.values();

    private SereneSeasonsBridge() {}

    static Season season(Level level) {
        return SEASONS[SeasonHelper.getSeasonState(level).getSeason().ordinal()];
    }

    static boolean isTropical(Level level, BlockPos pos) {
        return SeasonHelper.usesTropicalSeasons(level.getBiome(pos));
    }
}
