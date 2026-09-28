package com.darkona.feathers.api.event;

import net.minecraft.world.entity.LivingEntity;

/**
 * An entity overspending into Strain, or paying the last of it back.
 */
public abstract class StrainEvent extends FeathersEvent {

    protected StrainEvent(LivingEntity entity) {
        super(entity);
    }

    public static final class Started extends StrainEvent {
        public Started(LivingEntity entity) {
            super(entity);
        }
    }

    public static final class Cleared extends StrainEvent {
        public Cleared(LivingEntity entity) {
            super(entity);
        }
    }
}
