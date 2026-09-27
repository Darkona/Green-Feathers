package com.darkona.feathersoffatigue.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.DoubleValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

/**
 * One section per supported mod. Each only does anything when its mod is installed, and can be turned off.
 */
public final class FeathersCompatConfig {

    public static final ModConfigSpec SPEC;

    /* Droplets of Thirst */
    public static final BooleanValue DROPLETS_OF_THIRST;
    public static final DoubleValue DROPLETS_OF_THIRST_REGEN_REDUCTION;
    public static final DoubleValue DROPLETS_OF_THIRST_QUENCH_BONUS;
    public static final DoubleValue DROPLETS_OF_THIRST_THIRST_PER_FEATHER;

    /* Serene Seasons */
    public static final BooleanValue SEASONS;
    public static final BooleanValue SEASONS_WINTER_COLD;
    public static final DoubleValue SEASONS_WINTER_MAX_TEMPERATURE;
    public static final BooleanValue SEASONS_SUMMER_HEAT;
    public static final DoubleValue SEASONS_SUMMER_MIN_TEMPERATURE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Droplets of Thirst (the continuation of Thirst Was Taken): thirst slows regeneration, being quenched speeds it up.",
                "Ignored for players whose thirst is off.").push("droplets_of_thirst");
        DROPLETS_OF_THIRST = builder.comment("Use Droplets of Thirst when it is installed.")
                .define("enabled", true);
        DROPLETS_OF_THIRST_REGEN_REDUCTION = builder.comment("Feathers per second lost per missing thirst point (20 points = full).")
                .defineInRange("regen_reduction_per_thirst_point", 0.02, 0.0, 20.0);
        DROPLETS_OF_THIRST_QUENCH_BONUS = builder.comment("Feathers per second gained per point of quenched (thirst saturation).")
                .defineInRange("regen_bonus_per_quench_point", 0.02, 0.0, 20.0);
        DROPLETS_OF_THIRST_THIRST_PER_FEATHER = builder.comment("Thirst points each regenerated feather costs. 0 = regenerating costs no thirst.")
                .defineInRange("thirst_per_regenerated_feather", 0.0, 0.0, 20.0);
        builder.pop();

        builder.comment("Serene Seasons: winter outdoors is cold, a summer day in the sun is hot.").push("serene_seasons");
        SEASONS = builder.comment("Use Serene Seasons when it is installed.")
                .define("enabled", true);
        SEASONS_WINTER_COLD = builder.comment("Being outdoors in winter applies Cold.")
                .define("winter_cold", true);
        SEASONS_WINTER_MAX_TEMPERATURE = builder.comment("Only in biomes cooler than this (deserts and jungles stay warm). Plains are 0.8.")
                .defineInRange("winter_cold_below_temperature", 1.0, -2.0, 4.0);
        SEASONS_SUMMER_HEAT = builder.comment("A summer day under the sun applies Heat in warm biomes.")
                .define("summer_heat", true);
        SEASONS_SUMMER_MIN_TEMPERATURE = builder.comment("Biome temperature from which summer sun applies Heat (lower than the normal hot_temperature).")
                .defineInRange("summer_heat_from_temperature", 0.8, -2.0, 4.0);
        builder.pop();

        SPEC = builder.build();
    }

    private FeathersCompatConfig() {}
}
