package com.darkona.feathers.mount;

import com.darkona.feathers.api.MountStats;
import com.darkona.feathers.config.FeathersServerConfig;

/**
 * A creature type's {@link MountStats} with the config filling the gaps, as plain numbers the tick reads without
 * unwrapping optionals. {@code FeathersServiceImpl.mountTuning} keeps one per type until data maps or the config
 * change.
 *
 * @param minFeathers the lower end of the stamina range a creature is born with
 * @param maxFeathers the upper end of that range
 */
public record MountTuning(double minFeathers, double maxFeathers, double regenPerSecond, double gallopFeathersPerSecond,
                          double gallopSpeed, double jumpFeathers) {

    public static MountTuning of(MountStats stats) {
        int min = stats.minFeathers().orElseGet(FeathersServerConfig.MOUNT_MIN_FEATHERS);
        int max = stats.maxFeathers().orElseGet(FeathersServerConfig.MOUNT_MAX_FEATHERS);
        return new MountTuning(Math.min(min, max), Math.max(min, max),
                stats.regenPerSecond().orElseGet(FeathersServerConfig.MOUNT_REGEN),
                stats.gallopFeathersPerSecond().orElseGet(FeathersServerConfig.MOUNT_GALLOP_FEATHERS_PER_SECOND),
                stats.gallopSpeed().orElseGet(FeathersServerConfig.MOUNT_GALLOP_SPEED),
                stats.jumpFeathers().orElseGet(FeathersServerConfig.MOUNT_JUMP_FEATHERS));
    }
}
