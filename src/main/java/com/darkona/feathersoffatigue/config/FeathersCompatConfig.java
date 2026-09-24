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

    /* Tough As Nails */
    public static final BooleanValue TAN;
    public static final BooleanValue TAN_TEMPERATURE;
    public static final BooleanValue TAN_COLD_IS_COLD;
    public static final BooleanValue TAN_WARM_IS_HOT;
    public static final DoubleValue TAN_SEVERE_HYPERTHERMIA;
    public static final BooleanValue TAN_THIRST;
    public static final DoubleValue TAN_THIRST_REDUCTION;
    public static final DoubleValue TAN_HYDRATION_BONUS;
    public static final DoubleValue TAN_THIRST_EXHAUSTION;

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

        builder.comment("Tough As Nails: its temperature drives Cold, Heat and Fatigue; its thirst drives regeneration.").push("tough_as_nails");
        TAN = builder.comment("Use Tough As Nails when it is installed.")
                .define("enabled", true);
        TAN_TEMPERATURE = builder.comment("Use its temperature (when its temperature is on).")
                .define("temperature", true);
        TAN_COLD_IS_COLD = builder.comment("COLD applies the Cold effect; off: only ICY does.")
                .define("cold_level_applies_cold", true);
        TAN_WARM_IS_HOT = builder.comment("WARM applies Heat too; off: only HOT does.")
                .define("warm_level_applies_heat", false);
        TAN_SEVERE_HYPERTHERMIA = builder.comment("Hyperthermia progress (0 to 1) at which the heat is severe and Fatigue applies.")
                .defineInRange("severe_hyperthermia", 0.5, 0.0, 1.0);
        TAN_THIRST = builder.comment("Use its thirst (when its thirst is on).")
                .define("thirst", true);
        TAN_THIRST_REDUCTION = builder.comment("Feathers per second lost per missing thirst point (20 = full).")
                .defineInRange("regen_reduction_per_thirst_point", 0.02, 0.0, 20.0);
        TAN_HYDRATION_BONUS = builder.comment("Feathers per second gained per point of hydration.")
                .defineInRange("regen_bonus_per_hydration_point", 0.02, 0.0, 20.0);
        TAN_THIRST_EXHAUSTION = builder.comment("Thirst exhaustion per regenerated feather (4.0 = one thirst point). 0 = free.")
                .defineInRange("thirst_exhaustion_per_regenerated_feather", 0.0, 0.0, 40.0);
        builder.pop();

        builder.comment("Serene Seasons: winter outdoors is cold, a summer day in the sun is hot. Ignored while a body-temperature",
                "mod (Tough As Nails) is in charge: it already counts seasons.").push("serene_seasons");
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
