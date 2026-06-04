package com.darkona.feathers.api.client;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The feather shapes a {@link FeatherStyle} can be drawn in, and the overlays it can put over its row. Each is a row of
 * 9x9 cells in a texture, counted from its top-left corner:
 * <ul>
 *     <li>a variant: full body, half body, full border, half border, full shine, half shine (columns 0 to 5). Bodies
 *     are grayscale (tinted with the body color), borders white (tinted with the border color), shines white with
 *     their own alpha (drawn as they are, over the body);</li>
 *     <li>an overlay: primary and accent (columns 0 and 1), grayscale, tinted with the style's overlay colors and
 *     drawn over every slot of the row.</li>
 * </ul>
 * Green Feathers' own live in {@link #SHEET}; register a texture and row of your own for a new shape. A texture laid out
 * like SHEET is {@link #SHEET_WIDTH}x{@link #SHEET_HEIGHT}; a texture of another size is registered with its size,
 * which must be a multiple of 8 on both sides (pad it with transparent pixels).
 */
public final class FeatherVariants {

    /** Green Feathers' sprite sheet. Row 0 holds the empty slot's fill (column 0). */
    public static final ResourceLocation SHEET = id("textures/gui/icons.png");
    /** SHEET's width in pixels: 6 columns of 9x9 cells, padded to a multiple of 8. */
    public static final int SHEET_WIDTH = 56;
    /** SHEET's height in pixels: 8 rows of 9x9 cells. */
    public static final int SHEET_HEIGHT = 72;

    /** The plain striped feather: the player's colors, heat, endurance, mounts and armor pieces. */
    public static final ResourceLocation FEATHER = id("feather");
    /** Ice-like, lighter at the top: cold and momentum. */
    public static final ResourceLocation CRYSTAL = id("crystal");
    /** The feather with a longer highlight and a fading trail: energized. */
    public static final ResourceLocation GLINT = id("glint");
    /** A single deep stripe and a lighter edge: strain. */
    public static final ResourceLocation STRAINED = id("strained");
    /** No stripes, shaded along the edge: weight with no color of its own. */
    public static final ResourceLocation PLAIN = id("plain");

    /** Frost over the row, with icicles: cold. */
    public static final ResourceLocation FROST = id("frost");
    /** Flames over the row: heat. */
    public static final ResourceLocation HEAT = id("heat");

    /**
     * Where a variant's or overlay's cells are: {@code row} counts 9-pixel cells from the top of {@code texture}, which
     * is {@code textureWidth}x{@code textureHeight} pixels.
     */
    public record Sprites(ResourceLocation texture, int row, int textureWidth, int textureHeight) {

        /** In a texture laid out like {@link FeatherVariants#SHEET}. */
        public Sprites(ResourceLocation texture, int row) {
            this(texture, row, SHEET_WIDTH, SHEET_HEIGHT);
        }
    }

    private static volatile Object2ObjectOpenHashMap<ResourceLocation, Sprites> variants = new Object2ObjectOpenHashMap<>();
    private static volatile Object2ObjectOpenHashMap<ResourceLocation, Sprites> overlays = new Object2ObjectOpenHashMap<>();

    private FeatherVariants() {}

    /** Adds a feather shape in a texture laid out like {@link #SHEET}, or replaces the one with that id. */
    public static void registerVariant(ResourceLocation id, ResourceLocation texture, int row) {
        registerVariant(id, texture, row, SHEET_WIDTH, SHEET_HEIGHT);
    }

    /**
     * Adds a feather shape in a {@code width}x{@code height} texture, or replaces the one with that id. Both sizes must
     * be multiples of 8, and the texture wide enough for the variant's six cells.
     */
    public static synchronized void registerVariant(ResourceLocation id, ResourceLocation texture, int row, int width, int height) {
        variants = with(variants, id, new Sprites(Objects.requireNonNull(texture), row, width, height), VARIANT_COLUMNS);
    }

    /** Adds an overlay in a texture laid out like {@link #SHEET}, or replaces the one with that id. */
    public static void registerOverlay(ResourceLocation id, ResourceLocation texture, int row) {
        registerOverlay(id, texture, row, SHEET_WIDTH, SHEET_HEIGHT);
    }

    /**
     * Adds an overlay in a {@code width}x{@code height} texture, or replaces the one with that id. Both sizes must be
     * multiples of 8, and the texture wide enough for the overlay's two cells.
     */
    public static synchronized void registerOverlay(ResourceLocation id, ResourceLocation texture, int row, int width, int height) {
        overlays = with(overlays, id, new Sprites(Objects.requireNonNull(texture), row, width, height), OVERLAY_COLUMNS);
    }

    public static @Nullable Sprites variant(ResourceLocation id) {
        return variants.get(id);
    }

    public static @Nullable Sprites overlay(ResourceLocation id) {
        return overlays.get(id);
    }

    private static final int CELL = 9;
    private static final int VARIANT_COLUMNS = 6;
    private static final int OVERLAY_COLUMNS = 2;

    private static Object2ObjectOpenHashMap<ResourceLocation, Sprites> with(Object2ObjectOpenHashMap<ResourceLocation, Sprites> map, ResourceLocation id, Sprites sprites, int columns) {
        int width = sprites.textureWidth(), height = sprites.textureHeight(), row = sprites.row();
        if (width <= 0 || height <= 0 || (width & 7) != 0 || (height & 7) != 0)
            throw new IllegalArgumentException("Texture size " + width + "x" + height + " isn't a multiple of 8");
        if (width < columns * CELL) throw new IllegalArgumentException("A " + width + " pixel wide texture has no room for " + columns + " cells");
        if (row < 0 || (row + 1) * CELL > height) throw new IllegalArgumentException("Row " + row + " is outside a " + width + "x" + height + " texture");
        Object2ObjectOpenHashMap<ResourceLocation, Sprites> next = new Object2ObjectOpenHashMap<>(map);
        next.put(Objects.requireNonNull(id), sprites);
        return next;
    }
}
