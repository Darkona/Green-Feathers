package com.darkona.feathers.api.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * An entity running out and becoming exhausted, or recovering from it.
 */
public abstract class ExhaustionEvent extends FeathersEvent {

    protected ExhaustionEvent(LivingEntity entity) {
        super(entity);
    }

    public static final class Exhausted extends ExhaustionEvent {
        public Exhausted(LivingEntity entity) {
            super(entity);
        }
    }

    public static final class Recovered extends ExhaustionEvent {
        public Recovered(LivingEntity entity) {
            super(entity);
        }
    }
}
