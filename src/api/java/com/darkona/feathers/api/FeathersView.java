package com.darkona.feathers.api;

/**
 * Read-only view of an entity's feathers. All amounts are stamina ({@link Stamina#PER_FEATHER} per feather) unless
 * a method says feathers. Views of entities without feathers report zero and never exhaust.
 */
public interface FeathersView {

    FeathersView NONE = new FeathersView() {};

    default boolean hasFeathers() {return false;}

    default int stamina() {return 0;}

    default int maxStamina() {return 0;}

    /**
     * Stamina that can be spent right now: stamina minus the armor weight, plus bonus stamina (e.g. Endurance).
     */
    default int availableStamina() {return 0;}

    /**
     * Armor weight, in feathers. Each point makes one feather unusable.
     */
    default int weight() {return 0;}

    /**
     * Stamina overspent into Strain, which regeneration pays back first.
     */
    default int strain() {return 0;}

    default int maxStrain() {return 0;}

    /**
     * Temporary stamina from bonuses such as the Endurance effect; spent before regular stamina.
     */
    default int bonusStamina() {return 0;}

    /**
     * Ticks left before regeneration resumes after spending.
     */
    default int regenDelay() {return 0;}

    /**
     * Stamina gained (positive) or lost (negative) through regeneration and drains on the last tick.
     */
    default int lastDelta() {return 0;}

    /**
     * Total stamina regenerated since the entity joined the level. Compare two readings to see how much was
     * regenerated in between, e.g. to charge hydration for it.
     */
    default long totalRegenerated() {return 0;}

    /**
     * Exhausted: spent everything and must recover a share of the bar before exerting again.
     */
    default boolean exhausted() {return false;}

    default RestState restState() {return RestState.NONE;}

    default double restMultiplier() {return 1.0;}

    default int feathers() {return Stamina.toFeathers(stamina());}

    default int maxFeathers() {return Stamina.toFeathers(maxStamina());}

    default int availableFeathers() {return Stamina.toFeathers(availableStamina());}

    default boolean strained() {return strain() > 0;}
}
