package com.darkona.feathers.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

/**
 * Base of every Green Feathers event. All are posted on {@code NeoForge.EVENT_BUS}, on the server thread, and only
 * when something changes: never once per tick.
 */
public abstract class FeathersEvent extends LivingEvent {

    protected FeathersEvent(LivingEntity entity) {
        super(entity);
    }
}
