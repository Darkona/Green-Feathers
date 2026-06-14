package com.darkona.feathers.api;

/**
 * How a one-off spend behaves.
 *
 * @param simulate          checks the spend without changing state or firing events
 * @param allowStrain       may overspend into Strain when feathers run out, if the server enables Strain
 * @param ignoreExhaustion  may spend while exhausted
 * @param regenDelayTicks   ticks without regeneration after spending. {@link #SERVER_DEFAULT} uses the config
 */
public record SpendOptions(boolean simulate, boolean allowStrain, boolean ignoreExhaustion, int regenDelayTicks) {

    /** Selects the delay from the server configuration. */
    public static final int SERVER_DEFAULT = -1;

    /** Standard gameplay options with Strain enabled and the configured regeneration delay. */
    public static final SpendOptions DEFAULT = new SpendOptions(false, true, false, SERVER_DEFAULT);

    /**
     * Enables simulation for this spend.
     *
     * @return a copy that does not change state or fire spend events
     */
    public SpendOptions simulated() {
        return new SpendOptions(true, allowStrain, ignoreExhaustion, regenDelayTicks);
    }

    /**
     * Prevents this spend from creating more Strain.
     *
     * @return a copy with Strain disabled
     */
    public SpendOptions withoutStrain() {
        return new SpendOptions(simulate, false, ignoreExhaustion, regenDelayTicks);
    }

    /**
     * Permits this spend while the entity is exhausted.
     *
     * @return a copy that ignores exhaustion
     */
    public SpendOptions ignoringExhaustion() {
        return new SpendOptions(simulate, allowStrain, true, regenDelayTicks);
    }

    /**
     * Sets the regeneration delay applied after a successful spend.
     *
     * @param ticks the delay in ticks, or {@link #SERVER_DEFAULT}
     * @return a copy with the requested delay
     */
    public SpendOptions withRegenDelay(int ticks) {
        return new SpendOptions(simulate, allowStrain, ignoreExhaustion, ticks);
    }
}
