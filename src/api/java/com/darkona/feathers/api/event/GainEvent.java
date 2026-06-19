package com.darkona.feathers.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Fires before the API grants stamina outside normal regeneration. Listeners can change or cancel the gain.
 */
@Cancelable
public final class GainEvent extends FeathersEvent {

    private final ResourceLocation source;
    private int amount;

    /**
     * Creates an event for a requested stamina gain.
     *
     * @param entity the entity receiving stamina
     * @param source the reason for the gain
     * @param amount the requested amount in stamina units
     */
    public GainEvent(LivingEntity entity, ResourceLocation source, int amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    /**
     * Gets the reason supplied by the caller.
     *
     * @return the gain source
     */
    public ResourceLocation getSource() {
        return source;
    }

    /**
     * Gets the amount that Green Feathers will grant.
     *
     * @return the current amount in stamina units
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Changes the amount that Green Feathers will grant.
     *
     * @param amount the replacement amount, clamped to zero or greater
     */
    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }
}
