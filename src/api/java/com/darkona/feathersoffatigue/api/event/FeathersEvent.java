package com.darkona.feathersoffatigue.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Base class for Feathers of Fatigue events. Feathers of Fatigue posts them on {@code MinecraftForge.EVENT_BUS} from the
 * server thread when the documented state change occurs.
 */
public abstract class FeathersEvent extends LivingEvent {

    /**
     * Creates a Feathers of Fatigue event for one entity.
     *
     * @param entity the affected entity
     */
    protected FeathersEvent(LivingEntity entity) {
        super(entity);
    }
}
