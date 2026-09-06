package com.darkona.feathersoffatigue.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fires before regeneration resumes after a spend delay, block, or drain. Cancellation pauses regeneration for the
 * current tick. Feathers of Fatigue fires the event again on the next eligible tick.
 */
public final class RegenEvent extends FeathersEvent implements ICancellableEvent {

    /**
     * Creates a regeneration-resume event.
     *
     * @param entity the entity whose regeneration is about to resume
     */
    public RegenEvent(LivingEntity entity) {
        super(entity);
    }
}
