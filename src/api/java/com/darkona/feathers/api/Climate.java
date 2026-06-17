package com.darkona.feathers.api;

/**
 * How the surroundings feel to an entity. Drives the Cold, Heat and Fatigue effects.
 */
public enum Climate {
    /** Cold enough for the Cold effect: slower regeneration. */
    COLD,
    /** Comfortable conditions with no climate effect. */
    NEUTRAL,
    /** Mild heat: the Heat effect, spending costs double. */
    HOT,
    /** Severe heat: Heat plus Fatigue, fewer max feathers. */
    SCORCHING;

    /**
     * Checks whether this climate applies the Heat effect.
     *
     * @return {@code true} for {@link #HOT} and {@link #SCORCHING}
     */
    public boolean isHot() {
        return this == HOT || this == SCORCHING;
    }
}
