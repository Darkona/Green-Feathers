package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.spi.Registrations;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * The HUD's feather styles by id, and the providers that pick the local player's. Feathers of Fatigue registers its own
 * styles under the ids below through the same calls: register a style with one of those ids to replace it. Resource
 * packs can recolor any registered style with {@code assets/feathers_of_fatigue/feather_styles.json}, which wins over what
 * code registers.
 * <p>
 * Registration is safe from either logical side because this class does not load client-only classes. Mods should
 * register styles and providers during setup.
 */
public final class FeatherStyles {

    /** The player's feathers, by the client config's feather color. */
    public static final Identifier GREEN = id("green");
    /** The built-in blue player style. */
    public static final Identifier BLUE = id("blue");
    /** The built-in white player style. */
    public static final Identifier WHITE = id("white");
    /** The player's feathers under Feathers of Fatigue's effects. */
    public static final Identifier COLD = id("cold");
    /** The built-in style for the Hot effect. */
    public static final Identifier HOT = id("hot");
    /** The built-in style for the Energized effect. */
    public static final Identifier ENERGIZED = id("energized");
    /** The built-in style for the Momentum effect. */
    public static final Identifier MOMENTUM = id("momentum");
    /** Strain feathers, over the empty slots. */
    public static final Identifier STRAIN = id("strain");
    /** Bonus feathers (Endurance), in rows above. */
    public static final Identifier ENDURANCE = id("endurance");
    /** Weight with no color of its own (no armor piece or colored weight source behind it). */
    public static final Identifier ARMOR = id("armor");
    /** The empty slots behind the feathers, and the same while exhausted. The body color fills the slot. */
    public static final Identifier EMPTY = id("empty");
    /** The built-in style for empty slots while exhausted. */
    public static final Identifier EXHAUSTED = id("exhausted");

    /** The priority Feathers of Fatigue's own states answer at: above it wins over them, below only applies without them. */
    public static final int STATUS_PRIORITY = 0;

    /**
     * A registered provider and its selection metadata.
     *
     * @param id       the provider identifier
     * @param priority the selection priority
     * @param provider the registered provider
     */
    public record ProviderEntry(Identifier id, int priority, FeatherStyleProvider provider) {}

    private static volatile Object2ObjectOpenHashMap<Identifier, FeatherStyle> styles = new Object2ObjectOpenHashMap<>();
    private static volatile ProviderEntry[] providers = new ProviderEntry[0];
    private static volatile int version;

    private FeatherStyles() {}

    /**
     * Adds a style, or replaces the one with that id.
     *
     * @param id    the style identifier
     * @param style the style to register
     */
    public static synchronized void registerStyle(Identifier id, FeatherStyle style) {
        Object2ObjectOpenHashMap<Identifier, FeatherStyle> next = new Object2ObjectOpenHashMap<>(styles);
        next.put(Objects.requireNonNull(id), Objects.requireNonNull(style));
        styles = next;
        version++;
    }

    /**
     * Adds a provider, or replaces the provider with the same id. Higher priorities run first. Equal priorities keep
     * their registration order.
     *
     * @param id       a stable identifier for the provider
     * @param priority its selection priority
     * @param provider the provider to register
     */
    public static synchronized void registerStyleProvider(Identifier id, int priority, FeatherStyleProvider provider) {
        ProviderEntry entry = new ProviderEntry(Objects.requireNonNull(id), priority, Objects.requireNonNull(provider));
        ProviderEntry[] next = Registrations.withEntry(providers, entry, ProviderEntry::id);
        // Stable: equal priorities keep their registration order.
        Arrays.sort(next, Comparator.comparingInt(ProviderEntry::priority).reversed());
        providers = next;
        version++;
    }

    /**
     * The style registered with this id, as code registered it (resource packs may still recolor it on screen), or
     * null.
     *
     * @param id the style identifier
     * @return the registered style, or {@code null}
     */
    public static @Nullable FeatherStyle get(Identifier id) {
        return styles.get(id);
    }

    /**
     * Selects the first style supplied by the registered providers.
     *
     * @param player   the local player
     * @param feathers the player's current client-side stamina view
     * @return the selected style id, or {@code null} to use the configured color
     */
    public static @Nullable Identifier select(Player player, FeathersView feathers) {
        for (ProviderEntry entry : providers) {
            Identifier id = entry.provider().styleFor(player, feathers);
            if (id != null) return id;
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

    /**
     * Gets the registration version for caches built from styles or providers.
     *
     * @return a value that changes after each registration
     */
    public static int version() {
        return version;
    }
}
