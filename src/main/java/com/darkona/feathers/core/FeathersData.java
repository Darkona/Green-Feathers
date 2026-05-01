package com.darkona.feathers.core;

import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.RestState;
import com.darkona.feathers.api.Stamina;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * An entity's feathers: the state behind {@link FeathersView}. Server-authoritative; {@link FeathersTicker} ticks it
 * and {@link FeathersServiceImpl} changes it. Per-tick paths iterate small lists by index and allocate nothing.
 * <p>
 * Saved: stamina, Strain, the regeneration delay, exhaustion, bonus pools and compat counters. Drains, regeneration
 * blocks and rest bonuses are transient: whoever set them keeps refreshing them.
 */
public final class FeathersData implements FeathersView, INBTSerializable<CompoundTag> {

    public static final long FOREVER = Long.MAX_VALUE;

    /** A pool of bonus stamina, spent before regular stamina. */
    static final class Bonus {
        final ResourceLocation source;
        int amount;
        long expiresAt;

        Bonus(ResourceLocation source, int amount, long expiresAt) {
            this.source = source;
            this.amount = amount;
            this.expiresAt = expiresAt;
        }
    }

    /** A continuous drain; fractions of a stamina carry over between ticks. */
    static final class Drain {
        final ResourceLocation source;
        double perTick;
        boolean allowStrain;
        boolean blocksRegen;
        int timeoutTicks;
        long lastRefresh;
        double carry;

        Drain(ResourceLocation source) {
            this.source = source;
        }
    }

    /** A regeneration block or a rest bonus: a value that holds until a game time. */
    static final class Timed {
        final ResourceLocation source;
        double value;
        long until;

        Timed(ResourceLocation source) {
            this.source = source;
        }
    }

    /* Saved */
    int stamina;
    int strain;
    int regenDelay;
    boolean exhausted;
    final ArrayList<Bonus> bonuses = new ArrayList<>(2);
    final Object2DoubleOpenHashMap<String> counters = new Object2DoubleOpenHashMap<>();

    /* Derived from attributes and equipment */
    int maxStamina;
    int maxStrain;
    int weight;
    double lastWeightMultiplier = Double.NaN;
    boolean initialized;
    /** Never loaded from a save: starts with full feathers once the maximum is known. */
    boolean fresh = true;

    /* Transient */
    final ArrayList<Drain> drains = new ArrayList<>(2);
    final ArrayList<Timed> regenBlocks = new ArrayList<>(2);
    final ArrayList<Timed> restBonuses = new ArrayList<>(1);
    double regenCarry;
    int lastDelta;
    long totalRegenerated;
    Climate climate = Climate.NEUTRAL;

    RestState restState = RestState.NONE;
    double restMultiplier = 1.0;
    double lastX, lastY, lastZ;
    int stillTicks;

    /* What the client last received, to sync only on visible changes */
    int syncedStamina = -1, syncedMax = -1, syncedStrain = -1, syncedMaxStrain = -1, syncedBonus = -1, syncedWeight = -1;
    boolean syncedExhausted, syncedDelayed;
    RestState syncedRest = RestState.NONE;
    boolean forceSync = true;

    /* FeathersView */

    @Override
    public boolean hasFeathers() {
        return true;
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
        return regularAvailable() + bonusStamina();
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
        int total = 0;
        for (int i = 0, n = bonuses.size(); i < n; i++) total += bonuses.get(i).amount;
        return total;
    }

    @Override
    public int regenDelay() {
        return regenDelay;
    }

    @Override
    public int lastDelta() {
        return lastDelta;
    }

    @Override
    public long totalRegenerated() {
        return totalRegenerated;
    }

    @Override
    public boolean exhausted() {
        return exhausted;
    }

    @Override
    public RestState restState() {
        return restState;
    }

    @Override
    public double restMultiplier() {
        return restMultiplier;
    }

    /* Paying */

    int regularAvailable() {
        return Math.max(0, stamina - Stamina.ofFeathers(weight));
    }

    /**
     * Room left in Strain, or 0 when the spend or the server doesn't allow it.
     */
    int strainRoom(boolean allowStrain, boolean strainEnabled) {
        return allowStrain && strainEnabled ? Math.max(0, maxStrain - strain) : 0;
    }

    /**
     * Whether {@code cost} can be paid, all or nothing: bonus pools, then stamina, then Strain.
     */
    boolean canPay(int cost, boolean allowStrain, boolean strainEnabled) {
        return (long) cost <= (long) bonusStamina() + regularAvailable() + strainRoom(allowStrain, strainEnabled);
    }

    /**
     * Pays {@code cost}; call {@link #canPay} first.
     *
     * @return the stamina that went into Strain
     */
    int pay(int cost) {
        int left = cost;
        for (int i = 0, n = bonuses.size(); i < n && left > 0; i++) {
            Bonus bonus = bonuses.get(i);
            int take = Math.min(bonus.amount, left);
            bonus.amount -= take;
            left -= take;
        }
        removeEmptyBonuses();

        int take = Math.min(regularAvailable(), left);
        stamina -= take;
        left -= take;

        strain += left;
        return left;
    }

    private void removeEmptyBonuses() {
        for (int i = bonuses.size() - 1; i >= 0; i--) {
            if (bonuses.get(i).amount <= 0) bonuses.remove(i);
        }
    }

    /**
     * Whether nothing is left to spend: no stamina, no bonus, and no Strain room for spends that allow it.
     */
    boolean isSpent(boolean strainEnabled) {
        return availableStamina() <= 0 && strainRoom(true, strainEnabled) <= 0;
    }

    /* Bonuses, drains, blocks */

    @Nullable
    Bonus bonus(ResourceLocation source) {
        for (int i = 0, n = bonuses.size(); i < n; i++) {
            if (bonuses.get(i).source.equals(source)) return bonuses.get(i);
        }
        return null;
    }

    void setBonus(ResourceLocation source, int amount, long expiresAt) {
        Bonus bonus = bonus(source);
        if (bonus == null) bonuses.add(new Bonus(source, amount, expiresAt));
        else {
            bonus.amount = amount;
            bonus.expiresAt = expiresAt;
        }
    }

    boolean removeBonus(ResourceLocation source) {
        for (int i = 0, n = bonuses.size(); i < n; i++) {
            if (bonuses.get(i).source.equals(source)) {
                bonuses.remove(i);
                return true;
            }
        }
        return false;
    }

    @Nullable
    Drain drain(ResourceLocation source) {
        for (int i = 0, n = drains.size(); i < n; i++) {
            if (drains.get(i).source.equals(source)) return drains.get(i);
        }
        return null;
    }

    @Nullable
    static Timed timed(ArrayList<Timed> list, ResourceLocation source) {
        for (int i = 0, n = list.size(); i < n; i++) {
            if (list.get(i).source.equals(source)) return list.get(i);
        }
        return null;
    }

    static void setTimed(ArrayList<Timed> list, ResourceLocation source, double value, long until) {
        Timed entry = timed(list, source);
        if (entry == null) {
            entry = new Timed(source);
            list.add(entry);
        }
        entry.value = value;
        entry.until = until;
    }

    static void removeTimed(ArrayList<Timed> list, ResourceLocation source) {
        for (int i = 0, n = list.size(); i < n; i++) {
            if (list.get(i).source.equals(source)) {
                list.remove(i);
                return;
            }
        }
    }

    /**
     * Drops entries whose time has passed.
     */
    void expire(long now) {
        for (int i = bonuses.size() - 1; i >= 0; i--) {
            if (bonuses.get(i).expiresAt <= now) bonuses.remove(i);
        }
        for (int i = regenBlocks.size() - 1; i >= 0; i--) {
            if (regenBlocks.get(i).until <= now) regenBlocks.remove(i);
        }
        for (int i = restBonuses.size() - 1; i >= 0; i--) {
            if (restBonuses.get(i).until <= now) restBonuses.remove(i);
        }
    }

    boolean isRegenBlocked() {
        if (regenDelay > 0 || !regenBlocks.isEmpty()) return true;
        for (int i = 0, n = drains.size(); i < n; i++) {
            if (drains.get(i).blocksRegen) return true;
        }
        return false;
    }

    /* Compat counters */

    public double getCounter(String name) {
        return counters.getDouble(name);
    }

    public void setCounter(String name, double value) {
        counters.put(name, value);
    }

    /* Saving */

    @Override
    public @NotNull CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stamina", stamina);
        tag.putInt("strain", strain);
        tag.putInt("regen_delay", regenDelay);
        tag.putBoolean("exhausted", exhausted);

        ListTag bonusList = new ListTag();
        for (Bonus bonus : bonuses) {
            CompoundTag entry = new CompoundTag();
            entry.putString("source", bonus.source.toString());
            entry.putInt("amount", bonus.amount);
            entry.putLong("expires_at", bonus.expiresAt);
            bonusList.add(entry);
        }
        tag.put("bonuses", bonusList);

        CompoundTag counterTag = new CompoundTag();
        for (Object2DoubleMap.Entry<String> e : counters.object2DoubleEntrySet()) {
            counterTag.putDouble(e.getKey(), e.getDoubleValue());
        }
        tag.put("counters", counterTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        stamina = tag.getInt("stamina");
        strain = tag.getInt("strain");
        regenDelay = tag.getInt("regen_delay");
        exhausted = tag.getBoolean("exhausted");

        bonuses.clear();
        for (Tag t : tag.getList("bonuses", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) t;
            ResourceLocation source = ResourceLocation.tryParse(entry.getString("source"));
            if (source != null) bonuses.add(new Bonus(source, entry.getInt("amount"), entry.getLong("expires_at")));
        }

        counters.clear();
        CompoundTag counterTag = tag.getCompound("counters");
        for (String key : counterTag.getAllKeys()) {
            counters.put(key, counterTag.getDouble(key));
        }
        // Max stamina and weight come from attributes and equipment on the first tick.
        initialized = false;
        fresh = false;
        forceSync = true;
    }
}
