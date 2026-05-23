package com.darkona.feathers.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Regeneration is about to resume after a pause (a spend's delay, a block, a drain). Cancel to keep it paused for
 * this tick; the event fires again on the next one while it stays cancelled.
 */
@Cancelable
public final class RegenEvent extends FeathersEvent {

    public RegenEvent(LivingEntity entity) {
        super(entity);
    }
}
