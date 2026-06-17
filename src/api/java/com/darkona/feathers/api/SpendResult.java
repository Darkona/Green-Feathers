package com.darkona.feathers.api;

import com.darkona.feathers.api.event.SpendEvent;

/**
 * Outcome of a spend or of a drain tick.
 */
public enum SpendResult {
    /** Paid (or, when simulating, would be paid). */
    OK,
    /** Not enough feathers, even counting bonus feathers and strain where allowed. Nothing was spent. */
    INSUFFICIENT,
    /** The entity is exhausted and must recover before exerting itself again. Nothing was spent. */
    EXHAUSTED,
    /** A {@link SpendEvent.Pre} listener cancelled it. Nothing was spent. */
    CANCELLED,
    /** The entity does not use feathers, so the requested action is allowed. */
    EXEMPT;

    /**
     * Checks whether the action that requested the spend may proceed.
     *
     * @return {@code true} for {@link #OK} and {@link #EXEMPT}
     */
    public boolean allowed() {
        return this == OK || this == EXEMPT;
    }
}
