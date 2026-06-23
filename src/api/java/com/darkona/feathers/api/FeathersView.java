package com.darkona.feathers.api;

/**
 * Read-only view of an entity's feathers. All amounts are stamina ({@link Stamina#PER_FEATHER} per feather) unless
 * a method says feathers. Views of entities without feathers report zero and never exhaust.
 */
public interface FeathersView {

    /** An immutable empty view for entities that do not use stamina. */
    FeathersView NONE = new FeathersView() {};

    /**
     * Checks whether this view belongs to an entity that uses stamina.
     *
     * @return {@code true} when the entity uses stamina
     */
    default boolean hasFeathers() {
        return false;
    }

    /**
     * Gets the current regular stamina.
     *
     * @return the current amount in stamina units
     */
    default int stamina() {
        return 0;
    }

    /**
     * Gets the current stamina capacity.
     *
     * @return the current capacity in stamina units
     */
    default int maxStamina() {
        return 0;
    }

    /**
     * Stamina that can be spent right now: stamina minus the armor weight, plus bonus stamina (e.g. Endurance).
     *
     * @return the currently spendable amount in stamina units
     */
    default int availableStamina() {
        return 0;
    }

    /**
     * Armor weight, in feathers. Each point makes one feather unusable.
     *
     * @return the current armor weight in feathers
     */
    default int weight() {
        return 0;
    }

    /**
     * Stamina overspent into strain, which regeneration pays back first.
     *
     * @return the current strain in stamina units
     */
    default int strain() {
        return 0;
    }

    /**
     * Gets the current strain capacity.
     *
     * @return the maximum strain in stamina units
     */
    default int maxStrain() {
        return 0;
    }

    /**
     * Gets temporary stamina from bonuses such as the Endurance effect. Bonus stamina is spent first.
     *
     * @return the current bonus stamina in stamina units
     */
    default int bonusStamina() {
        return 0;
    }

    /**
     * Ticks left before regeneration resumes after spending.
     *
     * @return the remaining delay in ticks
     */
    default int regenDelay() {
        return 0;
    }

    /**
     * Stamina gained (positive) or lost (negative) through regeneration and drains on the last tick.
     *
     * @return the last tick's change in stamina units
     */
    default int lastDelta() {
        return 0;
    }

    /**
     * Total stamina regenerated since the entity joined the level. Compare two readings to see how much was
     * regenerated in between, e.g. to charge hydration for it.
     *
     * @return the accumulated regenerated stamina
     */
    default long totalRegenerated() {
        return 0;
    }

    /**
     * Exhausted: spent everything and must recover a share of the bar before exerting again.
     *
     * @return whether the entity is currently exhausted
     */
    default boolean exhausted() {
        return false;
    }

    /**
     * Gets the detected rest state.
     *
     * @return the entity's current kind of rest
     */
    default RestState restState() {
        return RestState.NONE;
    }

    /**
     * Gets the effective rest bonus.
     *
     * @return the current multiplier for strain recovery
     */
    default double restMultiplier() {
        return 1.0;
    }

    /**
     * Gets regular stamina in the display unit.
     *
     * @return the current amount in whole feathers, rounded down
     */
    default int feathers() {
        return Stamina.toFeathers(stamina());
    }

    /**
     * Gets the stamina capacity in the display unit.
     *
     * @return the current capacity in whole feathers, rounded down
     */
    default int maxFeathers() {
        return Stamina.toFeathers(maxStamina());
    }

    /**
     * Gets spendable stamina in the display unit.
     *
     * @return the currently spendable amount in whole feathers, rounded down
     */
    default int availableFeathers() {
        return Stamina.toFeathers(availableStamina());
    }

    /**
     * Checks whether the entity has accumulated strain.
     *
     * @return {@code true} when {@link #strain()} is greater than zero
     */
    default boolean strained() {
        return strain() > 0;
    }
}
