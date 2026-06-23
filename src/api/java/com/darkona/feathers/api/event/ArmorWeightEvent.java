package com.darkona.feathers.api.event;

import com.darkona.feathers.api.WeightSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * Fired when an entity's armor weight is recalculated (equipment changes, the armor weight multiplier changes,
 * config or datapack reloads), with the worn pieces plus every {@link WeightSource}.
 * Change it for custom rules; the armor weight multiplier attribute applies afterwards. For a plain percentage,
 * prefer an attribute modifier on {@code greenfeathers:armor_weight_multiplier}.
 */
public final class ArmorWeightEvent extends FeathersEvent {

    private double weight;

    /**
     * Creates the event with the calculated weight before the attribute multiplier.
     *
     * @param entity the entity whose weight is being calculated
     * @param weight the initial weight in feathers
     */
    public ArmorWeightEvent(LivingEntity entity, double weight) {
        super(entity);
        this.weight = weight;
    }

    /**
     * Gets the weight that Green Feathers will use.
     *
     * @return the current weight in feathers
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Changes the weight that Green Feathers will use.
     *
     * @param weight the replacement weight in feathers, clamped to zero or greater
     */
    public void setWeight(double weight) {
        this.weight = Math.max(0, weight);
    }
}
