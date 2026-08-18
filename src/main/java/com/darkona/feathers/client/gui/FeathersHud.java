package com.darkona.feathers.client.gui;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.client.FeatherAnimation;
import com.darkona.feathers.api.client.FeatherAnimations;
import com.darkona.feathers.api.client.FeatherStyle;
import com.darkona.feathers.api.client.FeatherStyles;
import com.darkona.feathers.api.client.FeatherVariants;
import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.client.SyncedFeathers;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.style.FeatherStylePack;
import com.darkona.feathers.weight.ArmorWeights;
import com.darkona.feathers.weight.WeightSplit;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import fuzs.overflowingbars.client.handler.RowCountRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.fml.ModList;

import static com.darkona.feathers.client.gui.Icons.*;

/**
 * Draws the feather row above the food bar and with other right-side bars. Each icon contains two feathers. Beyond a
 * full row, extra feathers use layered overflow colors and a row count. Gray icons show armor weight, red icons show
 * strain, and golden rows show bonus stamina. While riding a supported mount, the row displays the mount's stamina.
 * <p>
 * Each feather uses grayscale sprites tinted with its style's body and border colors. A style can also add a row
 * overlay. Providers or the client setting select the player's style. Fixed ids select the other styles. The HUD
 * resolves styles and animation once per client tick.
 */
public final class FeathersHud {

    public static final String OVERLAY_ID = "feathers";
    private static final ResourceLocation ICONS = FeatherVariants.SHEET;
    private static final int ICONS_PER_ROW = 10;
    private static final int FEATHERS_PER_ROW = ICONS_PER_ROW * 2;
    private static final int ROW_HEIGHT = 10;
    /** Disabled after the installed Overflowing Bars version rejects the expected renderer API. */
    private static boolean overflowingBars = ModList.get().isLoaded("overflowingbars");

    private static final ClientFeathersData DATA = ClientFeathersData.INSTANCE;

    /* Animation state, advanced once per client tick by tickAnimations and cleared by reset. */
    private static final int UNKNOWN_FEATHERS = -1;
    private static int previousFeathers = UNKNOWN_FEATHERS;
    private static int regenFlashTicks;
    private static int fullTicks;
    private static float alpha = 1.0f;
    /** The row's animation: its kind (null for none), and where it is. */
    private static FeatherAnimation.Kind motion;
    private static float motionSpeed;
    private static int motionAmplitude;
    private static float wave;
    private static float shakeClock;
    private static float pulsePhase;
    /** How far toward white the feathers are brightened this tick (PULSE). */
    private static float pulse;

    /* Styles, resolved once per client tick by refreshStyles. */
    private static final FeatherStyle FALLBACK = FeatherStyle.opaque(0x00B53A, 0x000000);
    private static final Look OWN = new Look().set(FALLBACK);
    private static final Look STRAIN = new Look().set(FALLBACK);
    private static final Look ENDURANCE = new Look().set(FALLBACK);
    private static final Look ARMOR = new Look().set(FALLBACK);
    private static final Look EMPTY = new Look().set(FALLBACK);
    private static final Look EXHAUSTED = new Look().set(FALLBACK);
    /** The plain feather, recolored per draw for texture-tinted feathers. */
    private static final Look TINTED = new Look().set(FALLBACK);
    /** The texture blit draws from, while drawing; null when something else may have bound its own. */
    private static ResourceLocation bound;
    /** FeatherColors' pairs are RGB: full alpha on top. */
    private static final int OPAQUE = 0xFF000000;

    private FeathersHud() {}

    /**
     * Back to a bar never drawn, on leaving a world: the next one starts without a regeneration flash or a fade.
     */
    public static void reset() {
        previousFeathers = UNKNOWN_FEATHERS;
        regenFlashTicks = 0;
        fullTicks = 0;
        alpha = 1.0f;
        motion = null;
        wave = 0;
        shakeClock = 0;
        pulsePhase = 0;
        pulse = 0;
    }

    public static void tickAnimations() {
        int feathers = DATA.feathers();
        // The first sync is no regeneration: the flash waits for a count the client already had.
        if (previousFeathers != UNKNOWN_FEATHERS && feathers > previousFeathers && FeathersClientConfig.REGEN_EFFECT.get()
                && regenFlashTicks <= 0) regenFlashTicks = 18;
        previousFeathers = DATA.hasFeathers() ? feathers : UNKNOWN_FEATHERS;
        if (regenFlashTicks > 0) regenFlashTicks--;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            refreshStyles(player);
            FeathersView mount = DATA.mount();
            LivingEntity mountEntity = shownMount(player, mount);
            animate(mountEntity != null ? FeatherAnimations.select(mountEntity, mount) : FeatherAnimations.select(player, DATA));
        }

        fullTicks = DATA.stamina() >= DATA.maxStamina() && DATA.bonusStamina() == 0 && DATA.strain() == 0 ? fullTicks + 1 : 0;
        if (FeathersClientConfig.FADE_WHEN_FULL.get() && fullTicks >= FeathersClientConfig.FADE_COOLDOWN.get()) {
            alpha = Math.max(0f, alpha - 1f / FeathersClientConfig.FADE_OUT_DURATION.get());
        } else {
            alpha = Math.min(1f, alpha + 1f / FeathersClientConfig.FADE_IN_DURATION.get());
        }
    }

    /** Starts, keeps or stops the row's animation and moves it one tick on. */
    private static void animate(FeatherAnimation animation) {
        FeatherAnimation.Kind kind = animation != null ? animation.kind() : null;
        if (kind != motion) {
            motion = kind;
            wave = 0;
            shakeClock = 0;
            pulsePhase = 0;
        }
        pulse = 0;
        if (kind == null) return;
        motionSpeed = animation.speed();
        motionAmplitude = Math.round(animation.amplitude());
        switch (kind) {
            // Past the row's end, a short pause before it starts again.
            case WAVE -> wave = wave >= ICONS_PER_ROW * ROW_HEIGHT ? -40 : wave + 2 * motionSpeed;
            case SHAKE -> shakeClock += motionSpeed;
            case PULSE -> {
                pulsePhase = (pulsePhase + motionSpeed * Mth.TWO_PI / 20f) % Mth.TWO_PI;
                pulse = Math.min(1f, animation.amplitude()) * (0.5f - 0.5f * Mth.cos(pulsePhase));
            }
        }
    }

    public static void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || !mc.gameMode.canHurtPlayer() || !(mc.getCameraEntity() instanceof LocalPlayer player)) return;
        if (!DATA.hasFeathers() || DATA.maxStamina() <= 0) return;

        boolean stack = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get();
        int x = screenWidth / 2 + 91 - 9 + FeathersClientConfig.X_OFFSET.get();
        int y = screenHeight - (stack ? gui.rightHeight : 49) + FeathersClientConfig.Y_OFFSET.get();

        // Riding a mount that has feathers, only the mount's matter: they replace the rider's, in the mount's colors.
        FeathersView mount = DATA.mount();
        LivingEntity vehicle = shownMount(player, mount);
        boolean riding = vehicle != null;
        FeathersView shown = riding ? mount : DATA;
        int bonusRows = bonusRows(shown);
        // Reserve space while faded out so the bars above remain stable.
        if (stack) gui.rightHeight += ROW_HEIGHT * (1 + bonusRows);

        if (alpha > 0) {
            gui.setupOverlayRenderState(true, false, ICONS);
            bound = ICONS;
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            if (riding) {
                long tint = FeatherColors.of(vehicle);
                drawRow(poseStack, mount, TINTED.colors(OPAQUE | FeatherColors.body(tint), OPAQUE | FeatherColors.outline(tint)), false, vehicle, x, y);
            } else {
                drawRow(poseStack, DATA, OWN, true, player, x, y);
            }
            drawBonus(poseStack, shown, x, y - ROW_HEIGHT, bonusRows);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        if (FeathersServerConfig.DEBUG_MODE.get()) drawDebug(poseStack, mc.font, player);
    }

    /** The creature the player rides, when its feathers are known and replace the player's row; else null. */
    private static LivingEntity shownMount(LocalPlayer player, FeathersView mount) {
        return player.getVehicle() instanceof LivingEntity living && mount.hasFeathers() && mount.maxStamina() > 0 ? living : null;
    }

    private static int bonusRows(FeathersView view) {
        return (Stamina.toFeathersCeil(view.bonusStamina()) + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW;
    }

    /**
     * One row of feathers in {@code look} (its overlay only when {@code overlay}). {@code wearer} colors the armor weight
     * by piece. A null wearer leaves the weight gray.
     */
    private static void drawRow(PoseStack poseStack, FeathersView view, Look look, boolean overlay, LivingEntity wearer, int x, int y) {
        int maxFeathers = view.maxFeathers();
        int feathers = view.feathers();
        int body = look.body;
        int border = look.border;

        // Empty slots up to the maximum (one row; higher maximums are layered over it). Reddish while exhausted.
        int backgroundIcons = Math.min(ICONS_PER_ROW, (maxFeathers + 1) / 2);
        drawSlots(poseStack, x, y, backgroundIcons, view.exhausted() ? EXHAUSTED : EMPTY);

        // Feathers, layered: the first row in its own color, every further row over it in a deeper shade of that color
        // (a lighter one for dark colors), outlined like the first.
        int layers = Math.max(1, (feathers + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW);
        for (int layer = 0; layer < layers; layer++) {
            int inLayer = Math.min(FEATHERS_PER_ROW, feathers - layer * FEATHERS_PER_ROW);
            int layerBody = layer == 0 ? body : (body & 0xFF000000) | FeatherColors.shade(body & 0xFFFFFF, layer);
            drawFeathers(poseStack, x, y, inLayer, layerBody, border, look);
        }

        // Strain: red feathers growing over the empty row.
        drawFeathers(poseStack, x, y, Math.min(FEATHERS_PER_ROW, Stamina.toFeathersCeil(view.strain())), STRAIN);

        // Armor weight: the first feathers are held back; spending stops when it reaches them.
        drawWeight(poseStack, view, wearer, x, y);

        // Frost, flames: over the whole row, like the old frozen feathers.
        if (overlay && look.overlay) drawOverlay(poseStack, x, y, backgroundIcons, look);

        if (view == DATA && regenFlashTicks >= 16) {
            for (int i = 0; i < backgroundIcons; i++) draw(poseStack, ICONS, FeatherVariants.SHEET_WIDTH, FeatherVariants.SHEET_HEIGHT, x, y, i, REGEN_OVERLAY_U, 0);
        }

        if (layers > 1) drawRowCount(poseStack, x, y, layers);
    }

    private static void drawBonus(PoseStack poseStack, FeathersView view, int x, int y, int rows) {
        int bonusFeathers = Stamina.toFeathersCeil(view.bonusStamina());
        for (int row = 0; row < rows; row++) {
            int inRow = Math.min(FEATHERS_PER_ROW, bonusFeathers - row * FEATHERS_PER_ROW);
            drawFeathers(poseStack, x, y - row * ROW_HEIGHT, inRow, ENDURANCE);
        }
    }

    private static final EquipmentSlot[] WEIGHT_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    /**
     * Armor weight from the right, head to feet: each piece's share in that piece's colors (leather in its dye), and
     * weight from other sources in gray.
     */
    private static void drawWeight(PoseStack poseStack, FeathersView view, LivingEntity wearer, int x, int y) {
        int weight = Math.min(FEATHERS_PER_ROW, view.weight());
        if (weight <= 0) return;
        if (wearer == null || !(view instanceof SyncedFeathers synced)) {
            drawFeathers(poseStack, x, y, weight, ARMOR);
            return;
        }
        int icons = (weight + 1) / 2;
        for (int i = 0; i < icons; i++) {
            int first = partOf(synced.weightSplit(), 2 * i);
            int second = 2 * i + 1 < weight ? partOf(synced.weightSplit(), 2 * i + 1) : -1;
            if (second < 0) {
                drawPart(poseStack, x, y, i, true, first, wearer, synced.weightSplit());
            } else {
                drawPart(poseStack, x, y, i, false, second, wearer, synced.weightSplit());
                if (second != first) drawPart(poseStack, x, y, i, true, first, wearer, synced.weightSplit());
            }
        }
    }

    /**
     * Which share the {@code feather}-th weight feather belongs to, in drawing order: the armor pieces
     * (ArmorWeights.HEAD to FEET), then the colored weight sources ({@link #SOURCE} + index), then the rest
     * (ArmorWeights.OTHER).
     */
    private static int partOf(WeightSplit split, int feather) {
        int end = 0;
        for (int part = ArmorWeights.HEAD; part <= ArmorWeights.FEET; part++) {
            end += split.part(part);
            if (feather < end) return part;
        }
        int sourceCount = split.sourceCount();
        for (int source = 0; source < sourceCount; source++) {
            end += split.feathers(source);
            if (feather < end) return SOURCE + source;
        }
        return ArmorWeights.OTHER;
    }

    /** Shares from here on are colored weight sources. */
    private static final int SOURCE = ArmorWeights.PARTS;

    private static void drawPart(PoseStack poseStack, int x, int y, int index, boolean half, int part, LivingEntity wearer, WeightSplit split) {
        if (part >= SOURCE) {
            int tint = split.tint(part - SOURCE);
            drawTinted(poseStack, x, y, index, half, tint >= 0 ? FeatherColors.ofColor(tint) : FeatherColors.of(Registry.ITEM.byId(-tint - 1)));
            return;
        }
        // A horse wears its armor in the chest slot.
        EquipmentSlot slot = part < WEIGHT_SLOTS.length ? WEIGHT_SLOTS[part] : null;
        ItemStack piece = slot != null ? wearer.getItemBySlot(slot) : ItemStack.EMPTY;
        if (piece.isEmpty()) drawIcons(poseStack, x, y, index, index + 1, half, ARMOR.body, ARMOR.border, ARMOR);
        else drawTinted(poseStack, x, y, index, half, FeatherColors.of(piece));
    }

    /** One plain feather in a FeatherColors pair: its body color, outlined in the complementary one. */
    private static void drawTinted(PoseStack poseStack, int x, int y, int index, boolean half, long tint) {
        drawIcons(poseStack, x, y, index, index + 1, half, OPAQUE | FeatherColors.body(tint), OPAQUE | FeatherColors.outline(tint), TINTED);
    }

    private static void drawFeathers(PoseStack poseStack, int x, int y, int count, Look look) {
        drawFeathers(poseStack, x, y, count, look.body, look.border, look);
    }

    /**
     * {@code count} feathers from the right, two per icon; an odd count ends in a half icon.
     */
    private static void drawFeathers(PoseStack poseStack, int x, int y, int count, int body, int border, Look sprites) {
        if (count <= 0) return;
        drawIcons(poseStack, x, y, 0, (count + 1) / 2, (count & 1) == 1, body, border, sprites);
    }

    /**
     * Icons {@code from} to {@code to} (exclusive) of {@code sprites}' variant, the last one half when
     * {@code halfLast}: all bodies in one color, all shines, then all borders in the other, so the color changes three
     * times per run instead of per icon. A border with alpha 0 is not drawn.
     */
    private static void drawIcons(PoseStack poseStack, int x, int y, int from, int to, boolean halfLast, int body, int border, Look sprites) {
        ResourceLocation sheet = sprites.sheet;
        int width = sprites.sheetWidth;
        int height = sprites.sheetHeight;
        int v = sprites.row * SIZE;
        int halfIcon = halfLast ? to - 1 : -1;
        setColor(pulse > 0 ? brighten(body) : body);
        for (int i = from; i < to; i++) draw(poseStack, sheet, width, height, x, y, i, i == halfIcon ? BODY_HALF_U : BODY_FULL_U, v);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        for (int i = from; i < to; i++) draw(poseStack, sheet, width, height, x, y, i, i == halfIcon ? SHINE_HALF_U : SHINE_FULL_U, v);
        if ((border >>> 24) != 0) {
            setColor(border);
            for (int i = from; i < to; i++) draw(poseStack, sheet, width, height, x, y, i, i == halfIcon ? BORDER_HALF_U : BORDER_FULL_U, v);
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        }
    }

    /** Empty slots: the fill in the style's body color, the outline (its variant's) in its border color. */
    private static void drawSlots(PoseStack poseStack, int x, int y, int icons, Look look) {
        setColor(look.body);
        for (int i = 0; i < icons; i++) draw(poseStack, look.slotSheet, look.slotWidth, look.slotHeight, x, y, i, EMPTY_U, 0);
        if ((look.border >>> 24) != 0) {
            setColor(look.border);
            for (int i = 0; i < icons; i++) draw(poseStack, look.sheet, look.sheetWidth, look.sheetHeight, x, y, i, BORDER_FULL_U, look.row * SIZE);
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    /** The style's overlay over {@code icons} slots: primary, then accent. */
    private static void drawOverlay(PoseStack poseStack, int x, int y, int icons, Look look) {
        int v = look.overlayRow * SIZE;
        setColor(look.overlayColor);
        for (int i = 0; i < icons; i++) draw(poseStack, look.overlaySheet, look.overlayWidth, look.overlayHeight, x, y, i, OVERLAY_U, v);
        setColor(look.overlayAccent);
        for (int i = 0; i < icons; i++) draw(poseStack, look.overlaySheet, look.overlayWidth, look.overlayHeight, x, y, i, OVERLAY_ACCENT_U, v);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    /** An ARGB color, its alpha times the fade. */
    private static void setColor(int argb) {
        RenderSystem.setShaderColor(FastColor.ARGB32.red(argb) / 255f, FastColor.ARGB32.green(argb) / 255f, FastColor.ARGB32.blue(argb) / 255f,
                FastColor.ARGB32.alpha(argb) / 255f * alpha);
    }

    /** The color moved toward white by the pulse. */
    private static int brighten(int argb) {
        int r = FastColor.ARGB32.red(argb);
        int g = FastColor.ARGB32.green(argb);
        int b = FastColor.ARGB32.blue(argb);
        r += (int) ((255 - r) * pulse);
        g += (int) ((255 - g) * pulse);
        b += (int) ((255 - b) * pulse);
        return FastColor.ARGB32.color(FastColor.ARGB32.alpha(argb), r, g, b);
    }

    /** Binds {@code sheet} only when it isn't the texture already bound (a style's own sprites, text, Overflowing Bars). */
    private static void draw(PoseStack poseStack, ResourceLocation sheet, int width, int height, int x, int y, int index, int u, int v) {
        if (sheet != bound) {
            RenderSystem.setShaderTexture(0, sheet);
            bound = sheet;
        }
        GuiComponent.blit(poseStack, x - index * 8, y + offset(index), u, v, SIZE, SIZE, width, height);
    }

    /**
     * How far the {@code index}-th icon is moved by the row's animation: up where the wave is, or a random jitter
     * that changes with the shake's clock (a hash of clock and index: no Random, nothing allocated).
     */
    private static int offset(int index) {
        if (motion == FeatherAnimation.Kind.WAVE) {
            return wave > index * ROW_HEIGHT && wave < (index + 1) * ROW_HEIGHT ? -motionAmplitude : 0;
        }
        if (motion == FeatherAnimation.Kind.SHAKE && motionAmplitude > 0) {
            int h = ((int) shakeClock * 0x9E3779B9) ^ (index * 0x85EBCA6B);
            h ^= h >>> 15;
            h *= 0x2C1B3C6D;
            h ^= h >>> 12;
            return (h & 0x7FFFFFFF) % (motionAmplitude + 1);
        }
        return 0;
    }

    /** "x2", "x3"...: drawn every frame, built once. */
    private static final String[] ROW_COUNTS = new String[64];

    static {
        for (int i = 0; i < ROW_COUNTS.length; i++) ROW_COUNTS[i] = "x" + i;
    }

    private static void drawRowCount(PoseStack poseStack, int x, int y, int layers) {
        Font font = Minecraft.getInstance().font;
        if (overflowingBars) {
            try {
                // Its count is value / maxRowCount: rows of one each, so the layer count comes out as is.
                RowCountRenderer.drawBarRowCount(poseStack, x + 18, y, layers, true, 1, font);
                // It binds its own texture and resets the shader color, fade included.
                bound = null;
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                return;
            } catch (LinkageError e) {
                overflowingBars = false;
                Feathers.LOGGER.warn("Overflowing Bars' row count renderer isn't compatible, using the built-in one", e);
            }
        }
        font.drawShadow(poseStack, layers < ROW_COUNTS.length ? ROW_COUNTS[layers] : "x" + layers, x + 11, y + 1, 0xFFFFFF);
        // Text rendering binds the font's texture.
        bound = null;
    }

    /**
     * The player's style: the first provider's answer, else the configured color. Then the fixed ones, resource
     * packs' changes included.
     */
    private static void refreshStyles(LocalPlayer player) {
        ResourceLocation chosen = FeatherStyles.select(player, DATA);
        FeatherStyle style = chosen != null ? FeatherStylePack.resolve(chosen) : null;
        OWN.set(style != null ? style : style(switch (FeathersClientConfig.FEATHER_COLOR.get()) {
            case GREEN -> FeatherStyles.GREEN;
            case BLUE -> FeatherStyles.BLUE;
            case WHITE -> FeatherStyles.WHITE;
        }));
        STRAIN.set(style(FeatherStyles.STRAIN));
        ENDURANCE.set(style(FeatherStyles.ENDURANCE));
        ARMOR.set(style(FeatherStyles.ARMOR));
        EMPTY.set(style(FeatherStyles.EMPTY));
        EXHAUSTED.set(style(FeatherStyles.EXHAUSTED));
        TINTED.set(FALLBACK);
    }

    private static FeatherStyle style(ResourceLocation id) {
        FeatherStyle style = FeatherStylePack.resolve(id);
        return style != null ? style : FALLBACK;
    }

    private static void drawDebug(PoseStack poseStack, Font font, LocalPlayer player) {
        int line = 2;
        font.drawShadow(poseStack, "Feathers %d/%d  stamina %d/%d  bonus %d  weight %d".formatted(DATA.feathers(), DATA.maxFeathers(),
                DATA.stamina(), DATA.maxStamina(), DATA.bonusStamina(), DATA.weight()), 2, line, 0xFFFFFF);
        line += 10;
        font.drawShadow(poseStack, "Strain %d/%d  regen delay %d  exhausted %s  rest %s".formatted(DATA.strain(), DATA.maxStrain(),
                DATA.regenDelay(), DATA.exhausted(), DATA.restState()), 2, line, 0xFF8080);
        line += 10;
        font.drawShadow(poseStack, "Regen %.2f f/s  usage x%.2f".formatted(FeathersAPI.getRegenPerSecond(player),
                FeathersAPI.getUsageMultiplier(player)), 2, line, 0xDDDD00);
        line += 10;
        ResourceLocation source = DATA.lastSpendSource();
        if (source != null) {
            font.drawShadow(poseStack, "Spent %d on %s".formatted(DATA.lastSpendCost(), source), 2, line, 0x80FF80);
        }
    }
}
