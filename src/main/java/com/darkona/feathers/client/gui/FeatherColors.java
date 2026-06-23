package com.darkona.feathers.client.gui;

import com.darkona.feathers.Feathers;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2LongOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;

import java.io.InputStream;

/**
 * Derives feather colors from a mount texture or armor icon. It adjusts the dominant color to remain readable and
 * pairs it with a complementary outline. The client calculates and caches each texture or item once.
 */
public final class FeatherColors {

    /** Neutral leather used when the client cannot read a texture. */
    public static final int LEATHER = 0x9A6A3F;

    private static final long UNKNOWN = Long.MIN_VALUE;
    private static final Object2LongOpenHashMap<ResourceLocation> BY_TEXTURE = new Object2LongOpenHashMap<>();
    private static final Reference2LongOpenHashMap<Item> BY_ITEM = new Reference2LongOpenHashMap<>();
    private static final Int2IntOpenHashMap SHADES = new Int2IntOpenHashMap();
    private static final Int2LongOpenHashMap BY_DYE = new Int2LongOpenHashMap();

    static {
        BY_TEXTURE.defaultReturnValue(UNKNOWN);
        BY_ITEM.defaultReturnValue(UNKNOWN);
        SHADES.defaultReturnValue(-1);
        BY_DYE.defaultReturnValue(UNKNOWN);
    }

    private FeatherColors() {}

    /** The body color packed in the high 32 bits of a pair, the outline color in the low ones. */
    public static int body(long pair) {
        return (int) (pair >>> 32);
    }

    public static int outline(long pair) {
        return (int) pair;
    }

    /**
     * Colors for a creature, from the texture its renderer draws.
     */
    public static long of(LivingEntity entity) {
        ResourceLocation texture;
        try {
            texture = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).getTextureLocation(entity);
        } catch (RuntimeException e) {
            texture = null;
        }
        // Some modded renderers have no texture to give: neutral leather, cached like any other.
        if (texture == null) texture = MissingTextureAtlasSprite.getLocation();
        long cached = BY_TEXTURE.getLong(texture);
        if (cached != UNKNOWN) return cached;
        long computed = pair(dominantOfTexture(texture));
        BY_TEXTURE.put(texture, computed);
        return computed;
    }

    /**
     * Colors for an item: a dyed item's own dye (leather armor), else its icon's dominant color.
     */
    public static long of(ItemStack stack) {
        Item item = stack.getItem();
        // 1.18.2 keeps a dye in the stack's display.color tag.
        if (item instanceof DyeableLeatherItem dyeable && dyeable.hasCustomColor(stack)) return ofColor(dyeable.getColor(stack));
        long cached = BY_ITEM.getLong(item);
        if (cached != UNKNOWN) return cached;
        long computed = pair(dominantOfItem(stack));
        BY_ITEM.put(item, computed);
        return computed;
    }

    /** A body color and an outline color, packed. */
    public static long pair(int body, int outline) {
        return (long) body << 32 | (outline & 0xFFFFFFFFL);
    }

    /**
     * The color of the {@code layer}-th row layered over a row of {@code rgb} feathers: the same hue, each layer a
     * step deeper, or a step lighter for colors already dark.
     */
    public static int shade(int rgb, int layer) {
        int key = (rgb & 0xFFFFFF) | Math.min(layer, 127) << 24;
        int cached = SHADES.get(key);
        if (cached != -1) return cached;
        float[] hsl = hsl(rgb);
        // Away from the base, and once that runs out of room, back past it to the other side, so layers never merge.
        float direction = hsl[2] >= 0.5f ? -1f : 1f;
        float lightness = hsl[2] + direction * 0.34f * layer;
        if (lightness < 0.19f || lightness > 0.87f) lightness = hsl[2] - direction * 0.25f * (layer - 1);
        lightness = Mth.clamp(lightness, 0.2f, 0.86f);
        int shaded = rgb(hsl[0], hsl[1], lightness);
        SHADES.put(key, shaded);
        return shaded;
    }

    /** Colors for a given color (a dye, a weight source's own): cached by color. */
    public static long ofColor(int rgb) {
        rgb &= 0xFFFFFF;
        long cached = BY_DYE.get(rgb);
        if (cached != UNKNOWN) return cached;
        long computed = pair(rgb);
        if (BY_DYE.size() >= 256) BY_DYE.clear();
        BY_DYE.put(rgb, computed);
        return computed;
    }

    /** Colors for an item without a stack at hand (a weight source's display item): its default stack's. */
    public static long of(Item item) {
        long cached = BY_ITEM.getLong(item);
        return cached != UNKNOWN ? cached : of(item.getDefaultInstance());
    }

    /** Resource packs changed: textures may have too. */
    public static void clear() {
        BY_TEXTURE.clear();
        BY_ITEM.clear();
        BY_DYE.clear();
    }

    private static int dominantOfTexture(ResourceLocation texture) {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (!manager.hasResource(texture)) return LEATHER;
        try (Resource resource = manager.getResource(texture); InputStream in = resource.getInputStream(); NativeImage image = NativeImage.read(in)) {
            return dominant(image);
        } catch (Exception e) {
            Feathers.LOGGER.debug("Couldn't read {} for its feather color", texture, e);
            return LEATHER;
        }
    }

    private static int dominantOfItem(ItemStack stack) {
        try {
            // The atlas keeps its sprites' images to itself in 1.18.2: read the sprite's texture file instead.
            ResourceLocation sprite = Minecraft.getInstance().getItemRenderer().getModel(stack, null, null, 0).getParticleIcon().getName();
            return dominantOfTexture(new ResourceLocation(sprite.getNamespace(), "textures/" + sprite.getPath() + ".png"));
        } catch (RuntimeException e) {
            return LEATHER;
        }
    }

    /**
     * The most common color, quantized to 4 bits per channel, averaged over the pixels that fall in it. Transparent
     * and pure black pixels do not contribute. This keeps a black horse very dark gray instead of pure black.
     */
    static int dominant(NativeImage image) {
        int[] counts = new int[4096];
        long[] red = new long[4096];
        long[] green = new long[4096];
        long[] blue = new long[4096];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                if ((abgr >>> 24) < 200) continue;
                int r = abgr & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                if (r + g + b < 24) continue;
                int bucket = (r >> 4) << 8 | (g >> 4) << 4 | b >> 4;
                counts[bucket]++;
                red[bucket] += r;
                green[bucket] += g;
                blue[bucket] += b;
            }
        }
        int best = -1;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] > 0 && (best < 0 || counts[i] > counts[best])) best = i;
        }
        if (best < 0) return LEATHER;
        int n = counts[best];
        return (int) (red[best] / n) << 16 | (int) (green[best] / n) << 8 | (int) (blue[best] / n);
    }

    /**
     * A readable body color and its complementary outline.
     */
    static long pair(int rgb) {
        float[] hsl = hsl(rgb);
        float lightness = Mth.clamp(hsl[2], 0.42f, 0.74f);
        // Apply a saturation floor only to clear colors. Near-grays from black or white horses remain gray.
        float saturation = hsl[1] > 0.2f ? Math.max(hsl[1], 0.28f) : hsl[1];
        int body = rgb(hsl[0], saturation, lightness);
        // Opposite hue, and the opposite side of the lightness band, so the edge contrasts in both.
        int outline = rgb((hsl[0] + 0.5f) % 1f, Math.max(saturation, 0.45f), lightness > 0.58f ? 0.22f : 0.82f);
        return pair(body, outline);
    }

    private static float[] hsl(int rgb) {
        float r = ((rgb >> 16) & 255) / 255f;
        float g = ((rgb >> 8) & 255) / 255f;
        float b = (rgb & 255) / 255f;
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float l = (max + min) / 2;
        float h = 0;
        float s = 0;
        if (max != min) {
            float d = max - min;
            s = l > 0.5f ? d / (2 - max - min) : d / (max + min);
            if (max == r) h = (g - b) / d + (g < b ? 6 : 0);
            else if (max == g) h = (b - r) / d + 2;
            else h = (r - g) / d + 4;
            h /= 6;
        }
        return new float[]{h, s, l};
    }

    private static int rgb(float h, float s, float l) {
        if (s == 0) {
            int v = Math.round(l * 255);
            return v << 16 | v << 8 | v;
        }
        float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
        float p = 2 * l - q;
        return Math.round(hue(p, q, h + 1f / 3) * 255) << 16 | Math.round(hue(p, q, h) * 255) << 8 | Math.round(hue(p, q, h - 1f / 3) * 255);
    }

    private static float hue(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6) return p + (q - p) * 6 * t;
        if (t < 0.5f) return q;
        if (t < 2f / 3) return p + (q - p) * (2f / 3 - t) * 6;
        return p;
    }
}
