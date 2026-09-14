package com.darkona.feathersoffatigue.api.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * Base class for events that report the start or end of strain.
 */
public abstract class StrainEvent extends FeathersEvent {

    /**
     * Creates a strain state event.
     *
     * @param entity the affected entity
     */
    protected StrainEvent(LivingEntity entity) {
        super(entity);
    }

    /** Reports that an entity started to accumulate strain. */
    public static final class Started extends StrainEvent {
        /**
         * Creates a strain-start event.
         *
         * @param entity the entity that started to accumulate strain
         */
        public Started(LivingEntity entity) {
            super(entity);
        }
    }

    /** Reports that an entity cleared all strain. */
    public static final class Cleared extends StrainEvent {
        /**
         * Creates a strain-cleared event.
         *
         * @param entity the entity that finished recovering from strain
         */
        public Cleared(LivingEntity entity) {
            super(entity);
        }
    }
}
