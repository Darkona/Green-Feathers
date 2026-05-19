package com.darkona.feathers.api;

/**
 * How the surroundings feel to an entity. Drives the Cold, Heat and Fatigue effects.
 */
public enum Climate {
    /** Cold enough for the Cold effect: slower regeneration. */
    COLD,
    NEUTRAL,
    /** Mild heat: the Heat effect, spending costs double. */
    HOT,
    /** Severe heat: Heat plus Fatigue, fewer max feathers. */
    SCORCHING;

    public boolean isHot() {
        return this == HOT || this == SCORCHING;
    }
}
