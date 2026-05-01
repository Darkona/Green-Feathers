package com.darkona.feathers.api;

/**
 * How a continuous drain behaves.
 *
 * @param allowStrain   may keep draining into Strain when feathers run out, if the server enables Strain
 * @param blocksRegen   regeneration pauses while the drain is active
 * @param timeoutTicks  the drain stops by itself this many ticks after the last {@code startDrain} call for it;
 *                      0 keeps it until {@code stopDrain}. Refresh it every tick while the activity lasts and a
 *                      mod that forgets to stop it can't leave the entity draining forever.
 */
public record DrainOptions(boolean allowStrain, boolean blocksRegen, int timeoutTicks) {

    public static final DrainOptions DEFAULT = new DrainOptions(true, true, 5);

    public DrainOptions withoutStrain() {
        return new DrainOptions(false, blocksRegen, timeoutTicks);
    }

    public DrainOptions keepingRegen() {
        return new DrainOptions(allowStrain, false, timeoutTicks);
    }

    public DrainOptions withTimeout(int ticks) {
        return new DrainOptions(allowStrain, blocksRegen, ticks);
    }
}
