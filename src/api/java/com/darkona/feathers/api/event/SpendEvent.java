package com.darkona.feathers.api.event;

import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * A one-off spend. Not fired for simulated spends or drains.
 */
public abstract class SpendEvent extends FeathersEvent {

    private final ResourceLocation source;

    protected SpendEvent(LivingEntity entity, ResourceLocation source) {
        super(entity);
        this.source = source;
    }

    /**
     * What the stamina is spent on, as given by the spender.
     */
    public ResourceLocation getSource() {
        return source;
    }

    /**
     * Before paying, after the usage multiplier and stamina modifiers. Change the cost or cancel.
     */
    @Cancelable
    public static final class Pre extends SpendEvent {
        private final SpendOptions options;
        private int cost;

        public Pre(LivingEntity entity, ResourceLocation source, int cost, SpendOptions options) {
            super(entity, source);
            this.cost = cost;
            this.options = options;
        }

        public int getCost() {
            return cost;
        }

        public void setCost(int cost) {
            this.cost = Math.max(0, cost);
        }

        public SpendOptions getOptions() {
            return options;
        }
    }

    /**
     * After a spend that went through.
     */
    public static final class Post extends SpendEvent {
        private final int cost;
        private final SpendResult result;

        public Post(LivingEntity entity, ResourceLocation source, int cost, SpendResult result) {
            super(entity, source);
            this.cost = cost;
            this.result = result;
        }

        public int getCost() {
            return cost;
        }

        /** Named apart from Forge's {@code Event#getResult}. */
        public SpendResult getSpendResult() {
            return result;
        }
    }
}
