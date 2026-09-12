package com.darkona.feathersoffatigue.api;

/**
 * How an entity is resting, as detected by Feathers of Fatigue. Resting speeds up recovering from strain.
 */
public enum RestState {
    /** Moving or otherwise active. */
    NONE,
    /** Standing still for a few seconds. */
    STILL,
    /** Crouching while standing still. */
    CROUCHING,
    /** Riding something: a boat, a mount, or a seat from another mod. */
    SITTING
}
