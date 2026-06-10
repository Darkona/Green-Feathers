package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The providers that animate the feather row. Like {@link FeatherStyles}, nothing here touches client classes.
 */
public final class FeatherAnimations {

    /** The priority Green Feathers' own triggers answer at: above it wins over them, below only applies without them. */
    public static final int STATUS_PRIORITY = 0;

    public record ProviderEntry(ResourceLocation id, int priority, FeatherAnimationProvider provider) {}

    private static volatile ProviderEntry[] providers = new ProviderEntry[0];

    private FeatherAnimations() {}

    /**
     * Adds a provider, or replaces the one with that id. Higher priorities are asked first; equal ones in the order
     * they were registered.
     */
    public static synchronized void registerProvider(ResourceLocation id, int priority, FeatherAnimationProvider provider) {
        ProviderEntry entry = new ProviderEntry(Objects.requireNonNull(id), priority, Objects.requireNonNull(provider));
        ProviderEntry[] current = providers;
        ProviderEntry[] next = null;
        for (int i = 0; i < current.length; i++) {
            if (current[i].id().equals(id)) {
                next = current.clone();
                next[i] = entry;
                break;
            }
        }
        if (next == null) {
            next = Arrays.copyOf(current, current.length + 1);
            next[current.length] = entry;
        }
        Arrays.sort(next, Comparator.comparingInt(ProviderEntry::priority).reversed());
        providers = next;
    }

    /**
     * The animation the providers pick for these feathers, or null when none answers.
     */
    public static @Nullable FeatherAnimation select(LivingEntity entity, FeathersView feathers) {
        for (ProviderEntry entry : providers) {
            FeatherAnimation animation = entry.provider().animationFor(entity, feathers);
            if (animation != null) return animation;
        }
        return null;
    }

    /** The registered providers, highest priority first. */
    public static ProviderEntry[] providers() {
        return providers.clone();
    }
}
