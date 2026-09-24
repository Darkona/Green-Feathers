package com.darkona.feathersoffatigue.style;

import com.darkona.feathersoffatigue.Feathers;
import com.darkona.feathersoffatigue.api.client.FeatherStyle;
import com.darkona.feathersoffatigue.api.client.FeatherStyles;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.Map;
import java.util.Optional;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * Resource packs' feather colors: {@code assets/feathers_of_fatigue/feather_styles.json} in every pack, lower packs first,
 * each entry laid field by field over the style code registered with that id. Read on resource reload; the resolved
 * styles are cached until the next reload or registration.
 * <pre>{@code
 * {"styles": {"feathers_of_fatigue:cold": {"border": "#1C4652", "overlay": "none"},
 *             "feathers_of_fatigue:strain": {"variant": "feathers_of_fatigue:feather", "sprites": "mypack:textures/gui/feathers.png"}}}
 * }</pre>
 */
public final class FeatherStylePack {

    public static final Identifier FILE = id("feather_styles.json");

    /** What one entry changes; missing fields keep the registered style's. An overlay named {@code none} removes it. */
    public record Patch(Optional<Integer> body, Optional<Integer> border, Optional<Identifier> variant, Optional<Identifier> overlay,
                        Optional<Integer> overlayColor, Optional<Integer> overlayAccent, Optional<Identifier> sprites) {

        public static final Codec<Patch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                FeatherStyle.COLOR_CODEC.optionalFieldOf("body").forGetter(Patch::body),
                FeatherStyle.COLOR_CODEC.optionalFieldOf("border").forGetter(Patch::border),
                Identifier.CODEC.optionalFieldOf("variant").forGetter(Patch::variant),
                Identifier.CODEC.optionalFieldOf("overlay").forGetter(Patch::overlay),
                FeatherStyle.COLOR_CODEC.optionalFieldOf("overlay_color").forGetter(Patch::overlayColor),
                FeatherStyle.COLOR_CODEC.optionalFieldOf("overlay_accent").forGetter(Patch::overlayAccent),
                Identifier.CODEC.optionalFieldOf("sprites").forGetter(Patch::sprites)
        ).apply(instance, Patch::new));

        /** This patch laid over {@code below}: its fields win. */
        public Patch over(Patch below) {
            return new Patch(body.or(below::body), border.or(below::border), variant.or(below::variant), overlay.or(below::overlay),
                    overlayColor.or(below::overlayColor), overlayAccent.or(below::overlayAccent), sprites.or(below::sprites));
        }

        /** The style with this patch applied; a new style (no registered one) needs a body. */
        public @Nullable FeatherStyle applyTo(@Nullable FeatherStyle base) {
            if (base == null) {
                if (body.isEmpty()) return null;
                base = new FeatherStyle(body.get(), FeatherStyle.BLACK);
            }
            Identifier overlayId = overlay.isPresent() ? (overlay.get().getPath().equals("none") ? null : overlay.get()) : base.overlay();
            return new FeatherStyle(body.orElse(base.body()), border.orElse(base.border()), variant.orElse(base.variant()), overlayId,
                    overlayColor.orElse(base.overlayColor()), overlayAccent.orElse(base.overlayAccent()), sprites.orElse(base.sprites()));
        }
    }

    private static final FeatherStyle MISSING = new FeatherStyle(0, 0);
    private static volatile Map<Identifier, Patch> patches = Map.of();
    private static final Object2ObjectOpenHashMap<Identifier, FeatherStyle> RESOLVED = new Object2ObjectOpenHashMap<>();
    private static int resolvedVersion = -1;

    private FeatherStylePack() {}

    /**
     * The style with this id as drawn: the registered one under the resource packs' changes. Null when neither
     * defines it.
     */
    public static synchronized @Nullable FeatherStyle resolve(Identifier id) {
        int version = FeatherStyles.version();
        if (version != resolvedVersion) {
            RESOLVED.clear();
            resolvedVersion = version;
        }
        FeatherStyle cached = RESOLVED.get(id);
        if (cached == null) {
            Patch patch = patches.get(id);
            FeatherStyle registered = FeatherStyles.get(id);
            cached = patch != null ? patch.applyTo(registered) : registered;
            if (cached == null) cached = MISSING;
            RESOLVED.put(id, cached);
        }
        return cached == MISSING ? null : cached;
    }

    /** Reads every pack's file, lowest first. */
    public static void load(ResourceManager manager) {
        Map<Identifier, Patch> merged = new Object2ObjectOpenHashMap<>();
        for (Resource resource : manager.getResourceStack(FILE)) {
            try (Reader reader = resource.openAsReader()) {
                merge(merged, parse(JsonParser.parseReader(reader), resource.sourcePackId()));
            } catch (Exception e) {
                Feathers.LOGGER.error("Couldn't read {} from {}", FILE, resource.sourcePackId(), e);
            }
        }
        setPatches(merged);
    }

    /** {@code higher}'s entries laid over {@code into}'s. */
    public static void merge(Map<Identifier, Patch> into, Map<Identifier, Patch> higher) {
        higher.forEach((id, patch) -> into.merge(id, patch, (below, above) -> above.over(below)));
    }

    /**
     * One file's entries. A broken entry is logged and skipped; the rest still apply.
     */
    public static Map<Identifier, Patch> parse(JsonElement json, String source) {
        Map<Identifier, Patch> parsed = new Object2ObjectOpenHashMap<>();
        if (!(json instanceof JsonObject root) || !(root.get("styles") instanceof JsonObject styles)) {
            Feathers.LOGGER.error("{} in {} needs a \"styles\" object", FILE, source);
            return parsed;
        }
        for (Map.Entry<String, JsonElement> entry : styles.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            if (id == null) {
                Feathers.LOGGER.error("{} in {}: \"{}\" isn't a style id", FILE, source, entry.getKey());
                continue;
            }
            Patch.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                    .ifSuccess(patch -> parsed.put(id, patch))
                    .ifError(error -> Feathers.LOGGER.error("{} in {}: {}: {}", FILE, source, id, error.message()));
        }
        return parsed;
    }

    public static synchronized void setPatches(Map<Identifier, Patch> next) {
        patches = Map.copyOf(next);
        RESOLVED.clear();
    }
}
