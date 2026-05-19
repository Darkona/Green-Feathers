package com.darkona.feathers.api.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * Stamina granted through the API (not regeneration). Change the amount or cancel.
 */
@Cancelable
public final class GainEvent extends FeathersEvent {

    private final ResourceLocation source;
    private int amount;

    public GainEvent(LivingEntity entity, ResourceLocation source, int amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    public ResourceLocation getSource() {
        return source;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = Math.max(0, amount);
    }
}
