package com.darkona.feathers.api.event;

import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Base class for one-time spend events. Simulated spends and continuous drains do not fire these events.
 */
public abstract class SpendEvent extends FeathersEvent {

    private final ResourceLocation source;

    /**
     * Creates a spend event for one source.
     *
     * @param entity the entity that requested the spend
     * @param source the reason for the spend
     */
    protected SpendEvent(LivingEntity entity, ResourceLocation source) {
        super(entity);
        this.source = source;
    }

    /**
     * Gets the action or reason supplied by the spender.
     *
     * @return the spend source
     */
    public ResourceLocation getSource() {
        return source;
    }

    /**
     * Fires before payment, after the usage multiplier and stamina modifiers. Listeners can change or cancel the cost.
     */
    public static final class Pre extends SpendEvent implements ICancellableEvent {
        private final SpendOptions options;
        private int cost;

        /**
         * Creates a pre-spend event.
         *
         * @param entity  the entity spending stamina
         * @param source  the reason for the spend
         * @param cost    the modified cost in stamina units
         * @param options the spend options
         */
        public Pre(LivingEntity entity, ResourceLocation source, int cost, SpendOptions options) {
            super(entity, source);
            this.cost = cost;
            this.options = options;
        }

        /**
         * Gets the cost that Green Feathers will charge.
         *
         * @return the current cost in stamina units
         */
        public int getCost() {
            return cost;
        }

        /**
         * Changes the cost that Green Feathers will charge.
         *
         * @param cost the replacement cost, clamped to zero or greater
         */
        public void setCost(int cost) {
            this.cost = Math.max(0, cost);
        }

        /**
         * Gets the options supplied for this spend.
         *
         * @return the immutable spend options
         */
        public SpendOptions getOptions() {
            return options;
        }
    }

    /**
     * Fires after a successful or exempt spend.
     */
    public static final class Post extends SpendEvent {
        private final int cost;
        private final SpendResult result;

        /**
         * Creates a post-spend event.
         *
         * @param entity the entity that requested the spend
         * @param source the reason for the spend
         * @param cost   the final cost in stamina units
         * @param result the completed spend result
         */
        public Post(LivingEntity entity, ResourceLocation source, int cost, SpendResult result) {
            super(entity, source);
            this.cost = cost;
            this.result = result;
        }

        /**
         * Gets the cost charged by the completed spend.
         *
         * @return the final cost in stamina units
         */
        public int getCost() {
            return cost;
        }

        /**
         * Gets the outcome of the completed spend.
         *
         * @return the completed spend result
         */
        public SpendResult getResult() {
            return result;
        }
    }
}
