package com.darkona.feathers.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * Base class for events that report the start or end of a continuous drain.
 */
public abstract class DrainEvent extends FeathersEvent {

    private final ResourceLocation source;

    /**
     * Creates a drain event for one source.
     *
     * @param entity the affected entity
     * @param source the drain source
     */
    protected DrainEvent(LivingEntity entity, ResourceLocation source) {
        super(entity);
        this.source = source;
    }

    /**
     * Gets the identifier supplied when the drain started.
     *
     * @return the drain source
     */
    public ResourceLocation getSource() {
        return source;
    }

    /** Reports that a continuous drain became active. */
    public static final class Started extends DrainEvent {
        private final double staminaPerTick;

        /**
         * Creates an event for a newly active drain.
         *
         * @param entity         the entity being drained
         * @param source         the drain source
         * @param staminaPerTick the configured rate in stamina units per tick
         */
        public Started(LivingEntity entity, ResourceLocation source, double staminaPerTick) {
            super(entity, source);
            this.staminaPerTick = staminaPerTick;
        }

        /**
         * Gets the rate supplied when the drain started.
         *
         * @return the configured rate in stamina units per tick
         */
        public double getStaminaPerTick() {
            return staminaPerTick;
        }
    }

    /** Reports that a continuous drain ended. */
    public static final class Stopped extends DrainEvent {

        /** Describes why a continuous drain ended. */
        public enum Reason {
            /** A caller explicitly stopped the drain. */
            STOPPED,
            /** Not refreshed within its timeout. */
            TIMED_OUT,
            /** Ran out of feathers (and of Strain, where allowed). */
            INSUFFICIENT,
            /** The entity became exhausted. */
            EXHAUSTED
        }

        private final Reason reason;

        /**
         * Creates an event for a drain that has ended.
         *
         * @param entity the entity that was being drained
         * @param source the drain source
         * @param reason why the drain ended
         */
        public Stopped(LivingEntity entity, ResourceLocation source, Reason reason) {
            super(entity, source);
            this.reason = reason;
        }

        /**
         * Gets the condition that ended the drain.
         *
         * @return why the drain ended
         */
        public Reason getReason() {
            return reason;
        }
    }
}
