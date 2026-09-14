package com.darkona.feathersoffatigue.api.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * Base class for events that report the start or end of exhaustion.
 */
public abstract class ExhaustionEvent extends FeathersEvent {

    /**
     * Creates an exhaustion state event.
     *
     * @param entity the affected entity
     */
    protected ExhaustionEvent(LivingEntity entity) {
        super(entity);
    }

    /** Reports that an entity became exhausted. */
    public static final class Exhausted extends ExhaustionEvent {
        /**
         * Creates an exhaustion-start event.
         *
         * @param entity the entity that became exhausted
         */
        public Exhausted(LivingEntity entity) {
            super(entity);
        }
    }

    /** Reports that an entity recovered from exhaustion. */
    public static final class Recovered extends ExhaustionEvent {
        /**
         * Creates an exhaustion-recovery event.
         *
         * @param entity the entity that recovered from exhaustion
         */
        public Recovered(LivingEntity entity) {
            super(entity);
        }
    }
}
