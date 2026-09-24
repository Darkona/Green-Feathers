package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.spi.Registrations;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The providers that animate the feather row. Like {@link FeatherStyles}, nothing here touches client classes.
 */
public final class FeatherAnimations {

    /** The priority Feathers of Fatigue's own triggers answer at: above it wins over them, below only applies without them. */
    public static final int STATUS_PRIORITY = 0;

    /**
     * A registered provider and its selection metadata.
     *
     * @param id       the provider identifier
     * @param priority the selection priority
     * @param provider the registered provider
     */
    public record ProviderEntry(Identifier id, int priority, FeatherAnimationProvider provider) {}

    private static volatile ProviderEntry[] providers = new ProviderEntry[0];

    private FeatherAnimations() {}

    /**
     * Adds a provider, or replaces the provider with the same id. Higher priorities run first. Equal priorities keep
     * their registration order.
     *
     * @param id       a stable identifier for the provider
     * @param priority its selection priority
     * @param provider the provider to register
     */
    public static synchronized void registerProvider(Identifier id, int priority, FeatherAnimationProvider provider) {
        ProviderEntry entry = new ProviderEntry(Objects.requireNonNull(id), priority, Objects.requireNonNull(provider));
        ProviderEntry[] next = Registrations.withEntry(providers, entry, ProviderEntry::id);
        Arrays.sort(next, Comparator.comparingInt(ProviderEntry::priority).reversed());
        providers = next;
    }

    /**
     * Selects the first animation supplied by the registered providers. Use this when rendering a feather row.
     *
     * @param entity   the entity represented by the row
     * @param feathers its current client-side stamina view
     * @return the selected animation, or {@code null} when the row should remain still
     */
    public static @Nullable FeatherAnimation select(LivingEntity entity, FeathersView feathers) {
        for (ProviderEntry entry : providers) {
            FeatherAnimation animation = entry.provider().animationFor(entity, feathers);
            if (animation != null) return animation;
        }
        return null;
    }

    /**
     * Gets a snapshot of the current provider order.
     *
     * @return a new array ordered from highest to lowest priority
     */
    public static ProviderEntry[] providers() {
        return providers.clone();
    }
}
