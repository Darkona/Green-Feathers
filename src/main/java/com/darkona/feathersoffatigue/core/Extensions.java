package com.darkona.feathersoffatigue.core;

import com.darkona.feathersoffatigue.api.ClimateProvider;
import com.darkona.feathersoffatigue.api.RegenFactor;
import com.darkona.feathersoffatigue.api.StaminaModifier;
import com.darkona.feathersoffatigue.api.WeightSource;
import com.darkona.feathersoffatigue.api.spi.Registrations;
import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Comparator;

/**
 * What other mods (and Feathers of Fatigue's own compats) plug in. Registration is rare and happens at startup; reads
 * happen every tick. So each kind lives in an array that registration replaces wholesale, read without locks or
 * iterators.
 */
public final class Extensions {

    public record ClimateEntry(Identifier id, int priority, ClimateProvider provider) {}

    public record RegenEntry(Identifier id, RegenFactor factor) {}

    public record WeightEntry(Identifier id, WeightSource source) {}

    public record ModifierEntry(Identifier id, int ordinal, StaminaModifier modifier) {}

    private static volatile ClimateEntry[] climates = new ClimateEntry[0];
    private static volatile RegenEntry[] regenFactors = new RegenEntry[0];
    private static volatile WeightEntry[] weightSources = new WeightEntry[0];
    private static volatile ModifierEntry[] modifiers = new ModifierEntry[0];

    private Extensions() {}

    public static ClimateEntry[] climates() {
        return climates;
    }

    public static RegenEntry[] regenFactors() {
        return regenFactors;
    }

    public static WeightEntry[] weightSources() {
        return weightSources;
    }

    public static ModifierEntry[] modifiers() {
        return modifiers;
    }

    public static synchronized void addClimate(Identifier id, int priority, ClimateProvider provider) {
        ClimateEntry[] next = Registrations.withEntry(climates, new ClimateEntry(id, priority, provider), ClimateEntry::id);
        Arrays.sort(next, Comparator.comparingInt(ClimateEntry::priority).reversed());
        climates = next;
    }

    public static synchronized void addRegenFactor(Identifier id, RegenFactor factor) {
        regenFactors = Registrations.withEntry(regenFactors, new RegenEntry(id, factor), RegenEntry::id);
    }

    public static synchronized void addWeightSource(Identifier id, WeightSource source) {
        weightSources = Registrations.withEntry(weightSources, new WeightEntry(id, source), WeightEntry::id);
    }

    public static synchronized void addModifier(Identifier id, int ordinal, StaminaModifier modifier) {
        ModifierEntry[] next = Registrations.withEntry(modifiers, new ModifierEntry(id, ordinal, modifier), ModifierEntry::id);
        Arrays.sort(next, Comparator.comparingInt(ModifierEntry::ordinal));
        modifiers = next;
    }
}
