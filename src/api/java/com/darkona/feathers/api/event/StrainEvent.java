package com.darkona.feathers.api.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * Base class for events that report the start or end of Strain.
 */
public abstract class StrainEvent extends FeathersEvent {

    /**
     * Creates a Strain state event.
     *
     * @param entity the affected entity
     */
    protected StrainEvent(LivingEntity entity) {
        super(entity);
    }

    /** Reports that an entity started to accumulate Strain. */
    public static final class Started extends StrainEvent {
        /**
         * Creates a Strain-start event.
         *
         * @param entity the entity that started to accumulate Strain
         */
        public Started(LivingEntity entity) {
            super(entity);
        }
    }

    /** Reports that an entity cleared all Strain. */
    public static final class Cleared extends StrainEvent {
        /**
         * Creates a Strain-cleared event.
         *
         * @param entity the entity that finished recovering from Strain
         */
        public Cleared(LivingEntity entity) {
            super(entity);
        }
    }
}
