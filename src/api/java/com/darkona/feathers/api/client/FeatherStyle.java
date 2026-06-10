package com.darkona.feathers.api.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

/**
 * How a row of feathers looks. The HUD draws every feather from grayscale sprites in the style's {@link #variant}: the
 * body tinted with {@link #body}, the variant's white shine, then the outline tinted with {@link #border}; an
 * {@link #overlay}, if any, goes over the whole row in its two colors. Colors are ARGB ({@code 0xAARRGGBB}); their
 * alpha multiplies with the HUD's fade. Rows layered over a full row use deeper shades of the body color, with the same
 * border.
 *
 * @param body          the feather's color, ARGB
 * @param border        the outline's color, ARGB; alpha 0 ({@link #NO_BORDER}) draws no outline
 * @param variant       the feather shape, a {@link FeatherVariants} id
 * @param overlay       a {@link FeatherVariants} overlay id drawn over the row, or null for none
 * @param overlayColor  the overlay's primary color, ARGB
 * @param overlayAccent the overlay's accent color, ARGB
 * @param sprites       a texture laid out like {@link FeatherVariants#SHEET} (56x72, the same cells) to take this
 *                      style's sprites from instead, or null. The colors still tint it: white body and border draw a
 *                      hand-colored sheet as is.
 */
public record FeatherStyle(int body, int border, ResourceLocation variant, @Nullable ResourceLocation overlay, int overlayColor, int overlayAccent,
                           @Nullable ResourceLocation sprites) {

    /** A border that isn't drawn. */
    public static final int NO_BORDER = 0;
    public static final int BLACK = 0xFF000000;
    public static final int WHITE = 0xFFFFFFFF;

    /** {@code "#RRGGBB"} (opaque) or {@code "#AARRGGBB"}. */
    public static final Codec<Integer> COLOR_CODEC = Codec.STRING.comapFlatMap(FeatherStyle::parseColor, FeatherStyle::formatColor);

    /**
     * {@code {"body": "#22A5F0", "border": "#000000", "variant": "greenfeathers:feather", "overlay": "greenfeathers:frost",
     * "overlay_color": "#8DC8FE", "overlay_accent": "#FFFFFF", "sprites": "mypack:textures/gui/feathers.png"}}: only
     * the body is required. The border defaults to black, the variant to the plain feather, the overlay colors to white.
     */
    public static final Codec<FeatherStyle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            COLOR_CODEC.fieldOf("body").forGetter(FeatherStyle::body),
            strictOptional("border", COLOR_CODEC, BLACK).forGetter(FeatherStyle::border),
            strictOptional("variant", ResourceLocation.CODEC, FeatherVariants.FEATHER).forGetter(FeatherStyle::variant),
            strictOptional("overlay", ResourceLocation.CODEC).forGetter(style -> Optional.ofNullable(style.overlay())),
            strictOptional("overlay_color", COLOR_CODEC, WHITE).forGetter(FeatherStyle::overlayColor),
            strictOptional("overlay_accent", COLOR_CODEC, WHITE).forGetter(FeatherStyle::overlayAccent),
            strictOptional("sprites", ResourceLocation.CODEC).forGetter(style -> Optional.ofNullable(style.sprites()))
    ).apply(instance, (body, border, variant, overlay, overlayColor, overlayAccent, sprites) ->
            new FeatherStyle(body, border, variant, overlay.orElse(null), overlayColor, overlayAccent, sprites.orElse(null))));

    public FeatherStyle {
        Objects.requireNonNull(variant, "variant");
    }

    /** The plain feather in these ARGB colors, without an overlay. */
    public FeatherStyle(int body, int border) {
        this(body, border, FeatherVariants.FEATHER, null, WHITE, WHITE, null);
    }

    /** Opaque colors from {@code 0xRRGGBB} values: the easy way to get the alpha right. */
    public static FeatherStyle opaque(int bodyRgb, int borderRgb) {
        return new FeatherStyle(0xFF000000 | bodyRgb, 0xFF000000 | borderRgb);
    }

    public FeatherStyle withBody(int body) {
        return new FeatherStyle(body, border, variant, overlay, overlayColor, overlayAccent, sprites);
    }

    public FeatherStyle withBorder(int border) {
        return new FeatherStyle(body, border, variant, overlay, overlayColor, overlayAccent, sprites);
    }

    public FeatherStyle withVariant(ResourceLocation variant) {
        return new FeatherStyle(body, border, variant, overlay, overlayColor, overlayAccent, sprites);
    }

    /** An overlay over the row, in two ARGB colors; null for none. */
    public FeatherStyle withOverlay(@Nullable ResourceLocation overlay, int color, int accent) {
        return new FeatherStyle(body, border, variant, overlay, color, accent, sprites);
    }

    public FeatherStyle withSprites(@Nullable ResourceLocation sprites) {
        return new FeatherStyle(body, border, variant, overlay, overlayColor, overlayAccent, sprites);
    }

    /**
     * An optional field that rejects a malformed value, as {@code optionalFieldOf} does from Minecraft 1.20.5 on; this
     * version's quietly drops it.
     */
    private static <A> MapCodec<Optional<A>> strictOptional(String name, Codec<A> codec) {
        return Codec.PASSTHROUGH.optionalFieldOf(name).flatXmap(
                value -> value.isPresent() ? codec.parse(value.get()).map(Optional::of) : DataResult.success(Optional.empty()),
                value -> value.isPresent()
                        ? codec.encodeStart(JsonOps.INSTANCE, value.get()).map(json -> Optional.<Dynamic<?>>of(new Dynamic<>(JsonOps.INSTANCE, json)))
                        : DataResult.success(Optional.empty()));
    }

    private static <A> MapCodec<A> strictOptional(String name, Codec<A> codec, A fallback) {
        return strictOptional(name, codec).xmap(value -> value.orElse(fallback), value -> value.equals(fallback) ? Optional.empty() : Optional.of(value));
    }

    public static DataResult<Integer> parseColor(String text) {
        String hex = text.startsWith("#") ? text.substring(1) : "";
        if (hex.length() != 6 && hex.length() != 8) return DataResult.error("Not a #RRGGBB or #AARRGGBB color: " + text);
        try {
            int value = Integer.parseUnsignedInt(hex, 16);
            return DataResult.success(hex.length() == 6 ? 0xFF000000 | value : value);
        } catch (NumberFormatException e) {
            return DataResult.error("Not a #RRGGBB or #AARRGGBB color: " + text);
        }
    }

    public static String formatColor(int argb) {
        return (argb >>> 24) == 0xFF ? "#%06X".formatted(argb & 0xFFFFFF) : "#%08X".formatted(argb);
    }
}
