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

    public ArmorWeightEvent(LivingEntity entity, double weight) {
        super(entity);
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = Math.max(0, weight);
    }
}
