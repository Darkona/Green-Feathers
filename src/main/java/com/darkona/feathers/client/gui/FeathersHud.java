package com.darkona.feathers.client.gui;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.client.SyncedFeathers;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersServerConfig;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.fml.ModList;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.client.gui.Icons.*;

/**
 * The feathers row, right above the food bar and stacked with the other right-side bars. Two feathers per icon;
 * past a full row, further feathers are drawn over it in the overflow color (layered), with a row count.
 * Grey icons mark feathers made unusable by armor weight, red ones Strain, golden rows bonus feathers. While riding a
 * mount that has feathers, the row shows the mount's instead, in the mount's own colors.
 */
public final class FeathersHud {

    public static final String OVERLAY_NAME = "Green Feathers";
    private static final ResourceLocation ICONS = id("textures/gui/icons.png");
    private static final int ICONS_PER_ROW = 10;
    private static final int FEATHERS_PER_ROW = ICONS_PER_ROW * 2;
    private static final int ROW_HEIGHT = 10;
    /** Off for good if the installed Overflowing Bars doesn't have the renderer this was built against. */
    private static boolean overflowingBars = ModList.get().isLoaded("overflowingbars");

    private static final ClientFeathersData DATA = ClientFeathersData.INSTANCE;

    /* Animation state, advanced once per client tick by tickAnimations. */
    private static int previousFeathers;
    private static int regenFlashTicks;
    private static int energizedWave;
    private static int fullTicks;
    private static float alpha = 1.0f;

    private FeathersHud() {}

    public static void tickAnimations() {
        int feathers = DATA.feathers();
        if (feathers > previousFeathers && FeathersClientConfig.REGEN_EFFECT.get() && regenFlashTicks <= 0) regenFlashTicks = 18;
        previousFeathers = feathers;
        if (regenFlashTicks > 0) regenFlashTicks--;

        LocalPlayer player = Minecraft.getInstance().player;
        energizedWave = player != null && FeathersAPI.isEnergized(player) ? (energizedWave >= 100 ? -40 : energizedWave + 2) : 0;

        fullTicks = DATA.stamina() >= DATA.maxStamina() && DATA.bonusStamina() == 0 && DATA.strain() == 0 ? fullTicks + 1 : 0;
        if (FeathersClientConfig.FADE_WHEN_FULL.get() && fullTicks >= FeathersClientConfig.FADE_COOLDOWN.get()) {
            alpha = Math.max(0f, alpha - 1f / FeathersClientConfig.FADE_OUT_DURATION.get());
        } else {
            alpha = Math.min(1f, alpha + 1f / FeathersClientConfig.FADE_IN_DURATION.get());
        }
    }

    public static void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || !mc.gameMode.canHurtPlayer() || !(mc.getCameraEntity() instanceof LocalPlayer player)) return;
        if (!DATA.hasFeathers() || DATA.maxStamina() <= 0) return;

        boolean stack = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get();
        int x = screenWidth / 2 + 91 - 9 + FeathersClientConfig.X_OFFSET.get();
        int y = screenHeight - (stack ? gui.right_height : 49) + FeathersClientConfig.Y_OFFSET.get();

        // Riding a mount that has feathers, only the mount's matter: they replace the rider's, in the mount's colors.
        FeathersView mount = DATA.mount();
        LivingEntity vehicle = player.getVehicle() instanceof LivingEntity living ? living : null;
        boolean riding = vehicle != null && mount.hasFeathers() && mount.maxStamina() > 0;
        FeathersView shown = riding ? mount : DATA;
        int bonusRows = bonusRows(shown);
        // Reserve the space even while faded out, so the bars above don't jump.
        if (stack) gui.right_height += ROW_HEIGHT * (1 + bonusRows);

        if (alpha > 0) {
            gui.setupOverlayRenderState(true, false, ICONS);
            RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
            if (riding) drawRow(poseStack, mount, null, FeatherColors.of(vehicle), vehicle, x, y);
            else drawRow(poseStack, DATA, iconSet(player), ownTint(), player, x, y);
            drawBonus(poseStack, shown, x, y - ROW_HEIGHT, bonusRows);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        if (FeathersServerConfig.DEBUG_MODE.get()) drawDebug(poseStack, mc.font, player);
    }

    private static int bonusRows(FeathersView view) {
        return (Stamina.toFeathersCeil(view.bonusStamina()) + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW;
    }

    /**
     * One row of feathers: in {@code set}'s sprites, or, when {@code set} is null, tinted with {@code tint} (a body
     * and outline color pair, see FeatherColors). {@code wearer} colors the armor weight by piece; null leaves it grey.
     */
    private static void drawRow(PoseStack poseStack, FeathersView view, Icons.Set set, long tint, LivingEntity wearer, int x, int y) {
        int maxFeathers = view.maxFeathers();
        int feathers = view.feathers();

        // Background up to the maximum (one row; higher maximums are layered over it). Reddish while exhausted.
        int backgroundIcons = Math.min(ICONS_PER_ROW, (maxFeathers + 1) / 2);
        if (view.exhausted()) RenderSystem.setShaderColor(1f, 0.55f, 0.55f, alpha);
        for (int i = 0; i < backgroundIcons; i++) draw(poseStack, x, y, i, NORMAL.background());
        if (view.exhausted()) RenderSystem.setShaderColor(1f, 1f, 1f, alpha);

        // Feathers, layered: the first row in its own color, every further row over it in a deeper shade of that color
        // (a lighter one for dark colors), outlined like the first: black for sprites, the complement for tinted rows.
        int layers = Math.max(1, (feathers + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW);
        int base = set != null ? set.color() : FeatherColors.body(tint);
        int edge = set != null ? 0 : FeatherColors.outline(tint);
        for (int layer = 0; layer < layers; layer++) {
            int inLayer = Math.min(FEATHERS_PER_ROW, feathers - layer * FEATHERS_PER_ROW);
            if (layer > 0) drawTintedFeathers(poseStack, x, y, inLayer, FeatherColors.pair(FeatherColors.shade(base, layer), edge));
            else if (set != null) drawFeathers(poseStack, x, y, inLayer, set);
            else drawTintedFeathers(poseStack, x, y, inLayer, tint);
        }

        // Strain: red feathers growing over the empty row.
        drawFeathers(poseStack, x, y, Math.min(FEATHERS_PER_ROW, Stamina.toFeathersCeil(view.strain())), STRAINED);

        // Armor weight: the first feathers are held back; spending stops when it reaches them.
        drawWeight(poseStack, view, wearer, x, y);

        if (view == DATA && regenFlashTicks >= 16) {
            for (int i = 0; i < backgroundIcons; i++) draw(poseStack, x, y, i, REGEN_OVERLAY);
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
    private static final long WHITE = (long) 0xF2F2F2 << 32 | 0x3C3C3C;

    /** Tinted colors for the configured feather color, or 0 when it has its own sprites. */
    private static long ownTint() {
        return FeathersClientConfig.FEATHER_COLOR.get() == FeathersClientConfig.FeatherColor.WHITE ? WHITE : 0;
    }

    /**
     * Armor weight from the right, head to feet: each piece's share in that piece's colors (leather in its dye), and
     * weight from other sources in grey.
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
        for (int source = 0, n = split.sourceCount(); source < n; source++) {
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
        if (piece.isEmpty()) draw(poseStack, x, y, index, half ? ARMOR.half() : ARMOR.full());
        else drawTinted(poseStack, x, y, index, half, FeatherColors.of(piece));
    }

    private static void drawTintedFeathers(PoseStack poseStack, int x, int y, int count, long tint) {
        if (count <= 0) return;
        int icons = (count + 1) / 2;
        for (int i = 0; i < icons; i++) {
            drawTinted(poseStack, x, y, i, i == icons - 1 && (count & 1) == 1, tint);
        }
    }

    /**
     * A grey feather tinted with the pair's body color, outlined in its complementary color.
     */
    private static void drawTinted(PoseStack poseStack, int x, int y, int index, boolean half, long tint) {
        setColor(FeatherColors.body(tint));
        draw(poseStack, x, y, index, half ? TINT_BODY_HALF : TINT_BODY_FULL);
        setColor(FeatherColors.outline(tint));
        draw(poseStack, x, y, index, half ? TINT_EDGE_HALF : TINT_EDGE_FULL);
        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
    }

    private static void setColor(int rgb) {
        RenderSystem.setShaderColor(((rgb >> 16) & 255) / 255f, ((rgb >> 8) & 255) / 255f, (rgb & 255) / 255f, alpha);
    }

    /**
     * {@code count} feathers from the right, two per icon; an odd count ends in a half icon.
     */
    private static void drawFeathers(PoseStack poseStack, int x, int y, int count, Icons.Set set) {
        if (count <= 0) return;
        int icons = (count + 1) / 2;
        for (int i = 0; i < icons; i++) {
            boolean half = i == icons - 1 && (count & 1) == 1;
            draw(poseStack, x, y, i, half ? set.half() : set.full());
        }
    }

    private static void draw(PoseStack poseStack, int x, int y, int index, GuiIcon icon) {
        int wave = energizedWave > index * ROW_HEIGHT && energizedWave < (index + 1) * ROW_HEIGHT ? 2 : 0;
        GuiComponent.blit(poseStack, x - index * 8, y - wave, icon.x(), icon.y(), icon.width(), icon.height(), 256, 256);
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
                RenderSystem.setShaderTexture(0, ICONS);
                RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
                return;
            } catch (LinkageError e) {
                overflowingBars = false;
                Feathers.LOGGER.warn("Overflowing Bars' row count renderer isn't compatible, using the built-in one", e);
            }
        }
        font.drawShadow(poseStack, layers < ROW_COUNTS.length ? ROW_COUNTS[layers] : "x" + layers, x + 11, y + 1, 0xFFFFFF);
        // Text rendering binds the font's texture.
        RenderSystem.setShaderTexture(0, ICONS);
    }

    private static Icons.Set iconSet(LocalPlayer player) {
        if (player.hasEffect(FeathersMobEffects.COLD.get())) return COLD;
        if (player.hasEffect(FeathersMobEffects.HOT.get())) return HOT;
        if (player.hasEffect(FeathersMobEffects.ENERGIZED.get())) return ENERGY;
        if (player.hasEffect(FeathersMobEffects.MOMENTUM.get())) return MOMENTUM;
        return switch (FeathersClientConfig.FEATHER_COLOR.get()) {
            case GREEN -> GREEN;
            case BLUE -> NORMAL;
            case WHITE -> null;
        };
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
