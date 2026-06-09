package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The HUD's feather styles by id, and the providers that pick the local player's. Green Feathers registers its own
 * styles under the ids below through the same calls: register a style with one of those ids to replace it. Resource
 * packs can recolor any registered style with {@code assets/greenfeathers/feather_styles.json}, which wins over what
 * code registers.
 * <p>
 * Safe to call from any side and at any time (registration is meant for mod setup); nothing here touches client
 * classes, so providers can be registered from common code.
 */
public final class FeatherStyles {

    /** The player's feathers, by the client config's feather color. */
    public static final ResourceLocation GREEN = id("green");
    public static final ResourceLocation BLUE = id("blue");
    public static final ResourceLocation WHITE = id("white");
    /** The player's feathers under Green Feathers' effects. */
    public static final ResourceLocation COLD = id("cold");
    public static final ResourceLocation HOT = id("hot");
    public static final ResourceLocation ENERGIZED = id("energized");
    public static final ResourceLocation MOMENTUM = id("momentum");
    /** Strain feathers, over the empty slots. */
    public static final ResourceLocation STRAIN = id("strain");
    /** Bonus feathers (Endurance), in rows above. */
    public static final ResourceLocation ENDURANCE = id("endurance");
    /** Weight with no color of its own (no armor piece or colored weight source behind it). */
    public static final ResourceLocation ARMOR = id("armor");
    /** The empty slots behind the feathers, and the same while exhausted. The body color fills the slot. */
    public static final ResourceLocation EMPTY = id("empty");
    public static final ResourceLocation EXHAUSTED = id("exhausted");

    /** The priority Green Feathers' own states answer at: above it wins over them, below only applies without them. */
    public static final int STATUS_PRIORITY = 0;

    public record ProviderEntry(ResourceLocation id, int priority, FeatherStyleProvider provider) {}

    private static volatile Object2ObjectOpenHashMap<ResourceLocation, FeatherStyle> styles = new Object2ObjectOpenHashMap<>();
    private static volatile ProviderEntry[] providers = new ProviderEntry[0];
    private static volatile int version;

    private FeatherStyles() {}

    /**
     * Adds a style, or replaces the one with that id.
     */
    public static synchronized void registerStyle(ResourceLocation id, FeatherStyle style) {
        Object2ObjectOpenHashMap<ResourceLocation, FeatherStyle> next = new Object2ObjectOpenHashMap<>(styles);
        next.put(Objects.requireNonNull(id), Objects.requireNonNull(style));
        styles = next;
        version++;
    }

    /**
     * Adds a provider, or replaces the one with that id. Higher priorities are asked first; equal ones in the order
     * they were registered.
     */
    public static synchronized void registerStyleProvider(ResourceLocation id, int priority, FeatherStyleProvider provider) {
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
        // Stable: equal priorities keep their registration order.
        Arrays.sort(next, Comparator.comparingInt(ProviderEntry::priority).reversed());
        providers = next;
        version++;
    }

    /**
     * The style registered with this id, as code registered it (resource packs may still recolor it on screen), or
     * null.
     */
    public static @Nullable FeatherStyle get(ResourceLocation id) {
        return styles.get(id);
    }

    /**
     * The style id the providers pick for this player, or null when none answers (the configured color then).
     */
    public static @Nullable ResourceLocation select(Player player, FeathersView feathers) {
        for (ProviderEntry entry : providers) {
            ResourceLocation id = entry.provider().styleFor(player, feathers);
            if (id != null) return id;
        }
        return null;
    }

    /** The registered providers, highest priority first. */
    public static ProviderEntry[] providers() {
        return providers.clone();
    }

    /** Changes with every registration: for caches built from the registered styles. */
    public static int version() {
        return version;
    }
}
