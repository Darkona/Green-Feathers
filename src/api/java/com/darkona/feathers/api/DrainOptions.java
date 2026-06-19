package com.darkona.feathers.api;

/**
 * How a continuous drain behaves.
 *
 * @param allowStrain   may keep draining into strain when feathers run out, if the server enables strain
 * @param blocksRegen   regeneration pauses while the drain is active
 * @param timeoutTicks  the drain stops this many ticks after its last {@code startDrain} call. A value of zero keeps
 *                      it active until {@code stopDrain}. Refreshing it each tick prevents a forgotten stop call from
 *                      leaving the entity with a permanent drain.
 */
public record DrainOptions(boolean allowStrain, boolean blocksRegen, int timeoutTicks) {

    /** Safe defaults for an activity that refreshes its drain each tick. */
    public static final DrainOptions DEFAULT = new DrainOptions(true, true, 5);

    /**
     * Prevents the drain from creating more strain.
     *
     * @return a copy with strain disabled
     */
    public DrainOptions withoutStrain() {
        return new DrainOptions(false, blocksRegen, timeoutTicks);
    }

    /**
     * Permits normal regeneration while the drain is active.
     *
     * @return a copy that does not block regeneration
     */
    public DrainOptions keepingRegen() {
        return new DrainOptions(allowStrain, false, timeoutTicks);
    }

    /**
     * Sets how long the drain can remain without a refresh.
     *
     * @param ticks the timeout in ticks, or zero to disable automatic timeout
     * @return a copy with the requested timeout
     */
    public DrainOptions withTimeout(int ticks) {
        return new DrainOptions(allowStrain, blocksRegen, ticks);
    }
}
