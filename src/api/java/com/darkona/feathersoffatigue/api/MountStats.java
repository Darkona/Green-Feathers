package com.darkona.feathersoffatigue.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

/**
 * Per-creature mount tuning, from the {@code feathers_of_fatigue:mount_stats} data map. Every field is optional and falls
 * back to the server config. An entry also makes the creature a mount, no tag needed. Example,
 * {@code data/feathers_of_fatigue/data_maps/entity_type/mount_stats.json}:
 * <pre>{@code
 * {"values": {"mymod:dragon": {"min_feathers": 40, "max_feathers": 60, "gallop_feathers_per_second": 0.05}}}
 * }</pre>
 *
 * @param minFeathers             lowest stamina a creature is born with
 * @param maxFeathers             highest stamina a creature is born with
 * @param regenPerSecond          feathers regenerated per second
 * @param gallopFeathersPerSecond feathers per second while ridden fast
 * @param gallopSpeed             horizontal speed, in blocks per tick, from which riding counts as galloping
 * @param jumpFeathers            feathers for a full-power jump, where the creature's jump is hooked
 */
public record MountStats(Optional<Integer> minFeathers, Optional<Integer> maxFeathers, Optional<Double> regenPerSecond,
                         Optional<Double> gallopFeathersPerSecond, Optional<Double> gallopSpeed, Optional<Double> jumpFeathers) {

    /** Empty tuning that delegates every value to the server configuration. */
    public static final MountStats DEFAULT = new MountStats(Optional.empty(), Optional.empty(), Optional.empty(),
            Optional.empty(), Optional.empty(), Optional.empty());

    /** The codec used by the {@code feathers_of_fatigue:mount_stats} entity type data map. */
    public static final Codec<MountStats> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.intRange(1, 1000).optionalFieldOf("min_feathers").forGetter(MountStats::minFeathers),
            Codec.intRange(1, 1000).optionalFieldOf("max_feathers").forGetter(MountStats::maxFeathers),
            Codec.doubleRange(0, 40).optionalFieldOf("regen_feathers_per_second").forGetter(MountStats::regenPerSecond),
            Codec.doubleRange(0, 40).optionalFieldOf("gallop_feathers_per_second").forGetter(MountStats::gallopFeathersPerSecond),
            Codec.doubleRange(0, 10).optionalFieldOf("gallop_speed").forGetter(MountStats::gallopSpeed),
            Codec.doubleRange(0, 40).optionalFieldOf("jump_feathers").forGetter(MountStats::jumpFeathers)
    ).apply(i, MountStats::new));
}
