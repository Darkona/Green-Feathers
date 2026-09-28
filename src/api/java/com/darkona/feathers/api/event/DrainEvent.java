package com.darkona.feathers.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

/**
 * A continuous drain starting or ending.
 */
public abstract class DrainEvent extends FeathersEvent {

    private final ResourceLocation source;

    protected DrainEvent(LivingEntity entity, ResourceLocation source) {
        super(entity);
        this.source = source;
    }

    public ResourceLocation getSource() {
        return source;
    }

    public static final class Started extends DrainEvent {
        private final double staminaPerTick;

        public Started(LivingEntity entity, ResourceLocation source, double staminaPerTick) {
            super(entity, source);
            this.staminaPerTick = staminaPerTick;
        }

        public double getStaminaPerTick() {
            return staminaPerTick;
        }
    }

    public static final class Stopped extends DrainEvent {

        public enum Reason {
            /** stopDrain was called. */
            STOPPED,
            /** Not refreshed within its timeout. */
            TIMED_OUT,
            /** Ran out of feathers (and of Strain, where allowed). */
            INSUFFICIENT,
            /** The entity became exhausted. */
            EXHAUSTED
        }

        private final Reason reason;

        public Stopped(LivingEntity entity, ResourceLocation source, Reason reason) {
            super(entity, source);
            this.reason = reason;
        }

        public Reason getReason() {
            return reason;
        }
    }
}
