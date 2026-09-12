package com.darkona.feathersoffatigue.api.client;

import java.util.Objects;

/**
 * How the feather row moves, like the vanilla hearts do.
 *
 * @param kind      what it does
 * @param speed     how fast, 1 being the default pace
 * @param amplitude how much: pixels for {@link Kind#WAVE} and {@link Kind#SHAKE}, and for {@link Kind#PULSE} how far
 *                  toward white the feathers brighten (0 to 1)
 */
public record FeatherAnimation(Kind kind, float speed, float amplitude) {

    /** The supported movement patterns for a feather row. */
    public enum Kind {
        /** A bump travelling along the row, like hearts under Regeneration. Default pace: 2 pixels a tick. */
        WAVE,
        /** Every feather jitters up and down at random, like hearts at low health. Default pace: new offsets every tick. */
        SHAKE,
        /** The feathers brighten and dim together. Default pace: once a second. */
        PULSE
    }

    /** The default wave used by built-in animation providers. */
    public static final FeatherAnimation WAVE = new FeatherAnimation(Kind.WAVE, 1f, 2f);
    /** The default shake used by built-in animation providers. */
    public static final FeatherAnimation SHAKE = new FeatherAnimation(Kind.SHAKE, 1f, 1f);
    /** The default pulse used by built-in animation providers. */
    public static final FeatherAnimation PULSE = new FeatherAnimation(Kind.PULSE, 1f, 0.45f);

    /**
     * Validates animation values when a new record is created.
     *
     * @throws NullPointerException     if {@code kind} is {@code null}
     * @throws IllegalArgumentException if either value is not finite, speed is not positive, or amplitude is negative
     */
    public FeatherAnimation {
        Objects.requireNonNull(kind, "kind");
        if (!Float.isFinite(speed) || !Float.isFinite(amplitude) || speed <= 0 || amplitude < 0) {
            throw new IllegalArgumentException("speed must be finite and positive, and amplitude must be finite and not negative");
        }
    }
}
