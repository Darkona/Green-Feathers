package com.darkona.feathers.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.RestState;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.network.SyncPayload;
import com.darkona.feathers.weight.ArmorWeights;

import java.util.Arrays;

/**
 * One entity's feathers as the client last heard them from the server. Client thread only.
 */
public class SyncedFeathers implements FeathersView {

    protected boolean synced;
    protected int stamina, maxStamina, strain, maxStrain, bonus, weight, regenDelay;
    protected boolean exhausted;
    protected RestState rest = RestState.NONE;
    protected final int[] weightParts = new int[ArmorWeights.PARTS];

    void apply(SyncPayload p) {
        synced = true;
        stamina = p.stamina();
        maxStamina = p.maxStamina();
        strain = p.strain();
        maxStrain = p.maxStrain();
        bonus = p.bonus();
        weight = p.weight();
        regenDelay = p.regenDelay();
        exhausted = p.exhausted();
        rest = p.rest();
        System.arraycopy(p.weightParts(), 0, weightParts, 0, weightParts.length);
    }

    /**
     * Feathers of weight from one part: head, chest, legs, feet, then other sources (see ArmorWeights).
     */
    public int weightPart(int part) {
        return weightParts[part];
    }

    void clear() {
        synced = false;
        stamina = maxStamina = strain = maxStrain = bonus = weight = regenDelay = 0;
        exhausted = false;
        rest = RestState.NONE;
        Arrays.fill(weightParts, 0);
    }

    void tick() {
        if (regenDelay > 0) regenDelay--;
    }

    @Override
    public boolean hasFeathers() {
        return synced;
    }

    @Override
    public int stamina() {
        return stamina;
    }

    @Override
    public int maxStamina() {
        return maxStamina;
    }

    @Override
    public int availableStamina() {
        return Math.max(0, stamina - Stamina.ofFeathers(weight)) + bonus;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public int strain() {
        return strain;
    }

    @Override
    public int maxStrain() {
        return maxStrain;
    }

    @Override
    public int bonusStamina() {
        return bonus;
    }

    @Override
    public int regenDelay() {
        return regenDelay;
    }

    @Override
    public boolean exhausted() {
        return exhausted;
    }

    @Override
    public RestState restState() {
        return rest;
    }
}
