package com.darkona.feathers.api;

/**
 * Units. Everything in the API counts <b>stamina</b>: a thousandth of a feather, so mods can charge fractions of a
 * feather and regeneration can be very slow without losing precision. A feather is half an icon on the HUD.
 */
public final class Stamina {

    public static final int PER_FEATHER = 1000;

    private Stamina() {}

    /**
     * Stamina in {@code feathers} feathers.
     */
    public static int ofFeathers(int feathers) {
        return feathers * PER_FEATHER;
    }

    /**
     * Stamina in {@code feathers} feathers, rounded to the nearest unit.
     */
    public static int ofFeathers(double feathers) {
        return (int) Math.round(feathers * PER_FEATHER);
    }

    /**
     * Whole feathers in {@code stamina}, rounded down.
     */
    public static int toFeathers(int stamina) {
        return Math.floorDiv(stamina, PER_FEATHER);
    }

    /**
     * Whole feathers in {@code stamina}, rounded up: what a HUD shows as partially filled.
     */
    public static int toFeathersCeil(int stamina) {
        return -Math.floorDiv(-stamina, PER_FEATHER);
    }

    /**
     * Stamina per tick for a rate in feathers per second.
     */
    public static double perTick(double feathersPerSecond) {
        return feathersPerSecond * PER_FEATHER / 20.0;
    }
}
