package com.darkona.feathersoffatigue.config;

// The FeathersCompatConfig fields and sections of the compats in src/disabled, as they are on Minecraft 1.21.1. Moved
// back into FeathersCompatConfig when one of them is enabled again: the fields with the others, each section in the
// order of the 1.21.1 file (Cold Sweat and Thirst Was Taken before Droplets of Thirst, Legendary Survival Overhaul
// before Serene Seasons).
final class DisabledCompatConfig {

    /* Cold Sweat */
    public static final BooleanValue COLD_SWEAT;
    public static final BooleanValue COLD_SWEAT_COLD;
    public static final BooleanValue COLD_SWEAT_HEAT;
    public static final IntValue COLD_SWEAT_COLD_THRESHOLD;
    public static final IntValue COLD_SWEAT_HOT_THRESHOLD;
    public static final IntValue COLD_SWEAT_SEVERE_THRESHOLD;

    /* Thirst Was Taken */
    public static final BooleanValue THIRST;
    public static final DoubleValue THIRST_REGEN_REDUCTION;
    public static final DoubleValue QUENCH_REGEN_BONUS;
    public static final DoubleValue THIRST_PER_FEATHER;

    /* Legendary Survival Overhaul */
    public static final BooleanValue LSO;
    public static final BooleanValue LSO_TEMPERATURE;
    public static final BooleanValue LSO_THIRST;
    public static final DoubleValue LSO_THIRST_REDUCTION;
    public static final DoubleValue LSO_HYDRATION_BONUS;
    public static final DoubleValue LSO_THIRST_EXHAUSTION;


    static {
        builder.comment("Cold Sweat: body temperature decides Cold, Heat and Fatigue instead of biomes.").push("cold_sweat");
        COLD_SWEAT = builder.comment("Use Cold Sweat when it is installed.")
                .define("enabled", true);
        COLD_SWEAT_COLD = builder.comment("Low body temperature applies Cold.")
                .define("cold_from_body_temperature", true);
        COLD_SWEAT_HEAT = builder.comment("High body temperature applies Heat, and severe heat Fatigue.")
                .define("heat_from_body_temperature", true);
        COLD_SWEAT_COLD_THRESHOLD = builder.comment("Body temperature at or below which Cold applies. Body temperature spans -150 to 150;",
                        "Cold Sweat's own freezing starts around -45.")
                .defineInRange("cold_threshold", -50, -150, 150);
        COLD_SWEAT_HOT_THRESHOLD = builder.comment("Body temperature at or above which Heat applies. Overheating starts around 45.")
                .defineInRange("hot_threshold", 50, -150, 150);
        COLD_SWEAT_SEVERE_THRESHOLD = builder.comment("Body temperature at or above which the heat is severe and Fatigue applies too.")
                .defineInRange("severe_hot_threshold", 100, -150, 150);
        builder.pop();

        builder.comment("Thirst Was Taken: thirst slows regeneration, being quenched speeds it up.").push("thirst_was_taken");
        THIRST = builder.comment("Use Thirst Was Taken when it is installed.")
                .define("enabled", true);
        THIRST_REGEN_REDUCTION = builder.comment("Feathers per second lost per missing thirst point (20 points = full).")
                .defineInRange("regen_reduction_per_thirst_point", 0.02, 0.0, 20.0);
        QUENCH_REGEN_BONUS = builder.comment("Feathers per second gained per point of quench (thirst saturation).")
                .defineInRange("regen_bonus_per_quench_point", 0.02, 0.0, 20.0);
        THIRST_PER_FEATHER = builder.comment("Thirst points each regenerated feather costs. 0 = regenerating costs no thirst.")
                .defineInRange("thirst_per_regenerated_feather", 0.0, 0.0, 20.0);
        builder.pop();

        builder.comment("Legendary Survival Overhaul: body temperature drives Cold, Heat and Fatigue; hydration drives regeneration.")
                .push("legendary_survival_overhaul");
        LSO = builder.comment("Use Legendary Survival Overhaul when it is installed.")
                .define("enabled", true);
        LSO_TEMPERATURE = builder.comment("Use its body temperature.")
                .define("temperature", true);
        LSO_THIRST = builder.comment("Use its hydration.")
                .define("thirst", true);
        LSO_THIRST_REDUCTION = builder.comment("Feathers per second lost per missing hydration point (20 = full).")
                .defineInRange("regen_reduction_per_thirst_point", 0.02, 0.0, 20.0);
        LSO_HYDRATION_BONUS = builder.comment("Feathers per second gained per point of hydration saturation.")
                .defineInRange("regen_bonus_per_saturation_point", 0.02, 0.0, 20.0);
        LSO_THIRST_EXHAUSTION = builder.comment("Thirst exhaustion per regenerated feather. 0 = free.")
                .defineInRange("thirst_exhaustion_per_regenerated_feather", 0.0, 0.0, 40.0);
        builder.pop();

    }
}
