package com.darkona.feathers.api;

/**
 * How a one-off spend behaves.
 *
 * @param simulate          only check: nothing changes, events don't fire
 * @param allowStrain       may overspend into Strain when feathers run out, if the server enables Strain
 * @param ignoreExhaustion  may spend while exhausted
 * @param regenDelayTicks   ticks without regeneration after spending; {@link #SERVER_DEFAULT} uses the config
 */
public record SpendOptions(boolean simulate, boolean allowStrain, boolean ignoreExhaustion, int regenDelayTicks) {

    public static final int SERVER_DEFAULT = -1;

    public static final SpendOptions DEFAULT = new SpendOptions(false, true, false, SERVER_DEFAULT);

    public SpendOptions simulated() {
        return new SpendOptions(true, allowStrain, ignoreExhaustion, regenDelayTicks);
    }

    public SpendOptions withoutStrain() {
        return new SpendOptions(simulate, false, ignoreExhaustion, regenDelayTicks);
    }

    public SpendOptions ignoringExhaustion() {
        return new SpendOptions(simulate, allowStrain, true, regenDelayTicks);
    }

    public SpendOptions withRegenDelay(int ticks) {
        return new SpendOptions(simulate, allowStrain, ignoreExhaustion, ticks);
    }
}
