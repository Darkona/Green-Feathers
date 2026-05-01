package com.darkona.feathers.client.gui;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import fuzs.overflowingbars.client.gui.RowCountRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.client.gui.Icons.*;

/**
 * The feathers row, right above the food bar and stacked with the other right-side bars. Two feathers per icon;
 * past a full row, further feathers are drawn over it in the overflow color (layered), with a row count.
 * Grey icons mark feathers made unusable by armor weight, red ones Strain, golden rows bonus feathers.
 */
public final class FeathersHud {

    public static final ResourceLocation LAYER_ID = id("feathers");
    private static final ResourceLocation ICONS = id("textures/gui/icons.png");
    private static final int ICONS_PER_ROW = 10;
    private static final int FEATHERS_PER_ROW = ICONS_PER_ROW * 2;
    private static final int ROW_HEIGHT = 10;
    private static final boolean OVERFLOWING_BARS = ModList.get().isLoaded("overflowingbars");

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

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.gameMode == null || !mc.gameMode.canHurtPlayer() || !(mc.getCameraEntity() instanceof LocalPlayer player)) return;
        if (!DATA.hasFeathers() || DATA.maxStamina() <= 0) return;

        Gui gui = mc.gui;
        boolean stack = FeathersClientConfig.AFFECTED_BY_RIGHT_HEIGHT.get();
        int x = graphics.guiWidth() / 2 + 91 - 9 + FeathersClientConfig.X_OFFSET.get();
        int y = graphics.guiHeight() - (stack ? gui.rightHeight : 49) + FeathersClientConfig.Y_OFFSET.get();

        int bonusRows = (Stamina.toFeathersCeil(DATA.bonusStamina()) + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW;
        // Reserve the space even while faded out, so the bars above don't jump.
        if (stack) gui.rightHeight += ROW_HEIGHT * (1 + bonusRows);

        if (alpha > 0) {
            RenderSystem.enableBlend();
            graphics.setColor(1f, 1f, 1f, alpha);
            drawRow(graphics, player, x, y);
            drawBonus(graphics, x, y - ROW_HEIGHT, bonusRows);
            graphics.setColor(1f, 1f, 1f, 1f);
            RenderSystem.disableBlend();
        }

        if (FeathersCommonConfig.DEBUG_MODE.get()) drawDebug(graphics, mc.font, player);
    }

    private static void drawRow(GuiGraphics graphics, LocalPlayer player, int x, int y) {
        int maxFeathers = DATA.maxFeathers();
        int feathers = DATA.feathers();
        Icons.Set set = iconSet(player);

        // Background up to the maximum (one row; higher maximums are layered over it). Reddish while exhausted.
        int backgroundIcons = Math.min(ICONS_PER_ROW, (maxFeathers + 1) / 2);
        if (DATA.exhausted()) graphics.setColor(1f, 0.55f, 0.55f, alpha);
        for (int i = 0; i < backgroundIcons; i++) draw(graphics, x, y, i, set.background());
        if (DATA.exhausted()) graphics.setColor(1f, 1f, 1f, alpha);

        // Feathers, layered: the first row in the state's color, every further row over it in the overflow color.
        int layers = Math.max(1, (feathers + FEATHERS_PER_ROW - 1) / FEATHERS_PER_ROW);
        for (int layer = 0; layer < layers; layer++) {
            int inLayer = Math.min(FEATHERS_PER_ROW, feathers - layer * FEATHERS_PER_ROW);
            drawFeathers(graphics, x, y, inLayer, layer == 0 ? set : OVERFLOW);
        }

        // Strain: red feathers growing over the empty row.
        drawFeathers(graphics, x, y, Math.min(FEATHERS_PER_ROW, Stamina.toFeathersCeil(DATA.strain())), STRAINED);

        // Armor weight: the first feathers turn grey; spending stops when it reaches them.
        drawFeathers(graphics, x, y, Math.min(FEATHERS_PER_ROW, DATA.weight()), ARMOR);

        if (regenFlashTicks >= 16) {
            for (int i = 0; i < backgroundIcons; i++) draw(graphics, x, y, i, REGEN_OVERLAY);
        }

        if (layers > 1) drawRowCount(graphics, x, y, layers);
    }

    private static void drawBonus(GuiGraphics graphics, int x, int y, int rows) {
        int bonusFeathers = Stamina.toFeathersCeil(DATA.bonusStamina());
        for (int row = 0; row < rows; row++) {
            int inRow = Math.min(FEATHERS_PER_ROW, bonusFeathers - row * FEATHERS_PER_ROW);
            drawFeathers(graphics, x, y - row * ROW_HEIGHT, inRow, ENDURANCE);
        }
    }

    /**
     * {@code count} feathers from the right, two per icon; an odd count ends in a half icon.
     */
    private static void drawFeathers(GuiGraphics graphics, int x, int y, int count, Icons.Set set) {
        if (count <= 0) return;
        int icons = (count + 1) / 2;
        for (int i = 0; i < icons; i++) {
            boolean half = i == icons - 1 && (count & 1) == 1;
            draw(graphics, x, y, i, half ? set.half() : set.full());
        }
    }

    private static void draw(GuiGraphics graphics, int x, int y, int index, GuiIcon icon) {
        int wave = energizedWave > index * ROW_HEIGHT && energizedWave < (index + 1) * ROW_HEIGHT ? 2 : 0;
        graphics.blit(ICONS, x - index * 8, y - wave, icon.x(), icon.y(), icon.width(), icon.height(), 256, 256);
    }

    private static void drawRowCount(GuiGraphics graphics, int x, int y, int layers) {
        Font font = Minecraft.getInstance().font;
        if (OVERFLOWING_BARS) {
            RowCountRenderer.drawBarRowCount(graphics, x + 18, y, layers, true, font);
        } else {
            graphics.drawString(font, "x" + layers, x + 11, y + 1, 0xFFFFFF);
        }
    }

    private static Icons.Set iconSet(LocalPlayer player) {
        if (player.hasEffect(FeathersMobEffects.COLD)) return COLD;
        if (player.hasEffect(FeathersMobEffects.HOT)) return HOT;
        if (player.hasEffect(FeathersMobEffects.ENERGIZED)) return ENERGY;
        if (player.hasEffect(FeathersMobEffects.MOMENTUM)) return MOMENTUM;
        return FeathersClientConfig.ALTERNATIVE_FEATHER_COLOR.get() ? GREEN : NORMAL;
    }

    private static void drawDebug(GuiGraphics graphics, Font font, LocalPlayer player) {
        int line = 2;
        graphics.drawString(font, "Feathers %d/%d  stamina %d/%d  bonus %d  weight %d".formatted(DATA.feathers(), DATA.maxFeathers(),
                DATA.stamina(), DATA.maxStamina(), DATA.bonusStamina(), DATA.weight()), 2, line, 0xFFFFFF);
        line += 10;
        graphics.drawString(font, "Strain %d/%d  regen delay %d  exhausted %s  rest %s".formatted(DATA.strain(), DATA.maxStrain(),
                DATA.regenDelay(), DATA.exhausted(), DATA.restState()), 2, line, 0xFF8080);
        line += 10;
        graphics.drawString(font, "Regen %.2f f/s  usage x%.2f".formatted(FeathersAPI.getRegenPerSecond(player),
                FeathersAPI.getUsageMultiplier(player)), 2, line, 0xDDDD00);
        line += 10;
        ResourceLocation source = DATA.lastSpendSource();
        if (source != null) {
            graphics.drawString(font, "Spent %d on %s".formatted(DATA.lastSpendCost(), source), 2, line, 0x80FF80);
        }
    }
}
