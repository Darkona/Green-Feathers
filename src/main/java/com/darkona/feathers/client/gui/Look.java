package com.darkona.feathers.client.gui;

import com.darkona.feathers.api.client.FeatherStyle;
import com.darkona.feathers.api.client.FeatherVariants;
import com.darkona.feathers.api.client.FeatherVariants.Sprites;
import net.minecraft.resources.ResourceLocation;

/**
 * A {@link FeatherStyle} resolved for drawing: its colors, where its variant's and overlay's cells are. Filled in place
 * (once per client tick, or per draw for texture-tinted feathers), never allocated while drawing.
 */
final class Look {

    private static final Sprites FALLBACK = new Sprites(FeatherVariants.SHEET, 1);

    int body;
    int border;
    /** The variant's texture, its size and cell row. */
    ResourceLocation sheet = FeatherVariants.SHEET;
    int sheetWidth = FeatherVariants.SHEET_WIDTH;
    int sheetHeight = FeatherVariants.SHEET_HEIGHT;
    int row = 1;
    /** Where the empty slot's fill is (row 0), and that texture's size. */
    ResourceLocation slotSheet = FeatherVariants.SHEET;
    int slotWidth = FeatherVariants.SHEET_WIDTH;
    int slotHeight = FeatherVariants.SHEET_HEIGHT;
    boolean overlay;
    ResourceLocation overlaySheet = FeatherVariants.SHEET;
    int overlayWidth = FeatherVariants.SHEET_WIDTH;
    int overlayHeight = FeatherVariants.SHEET_HEIGHT;
    int overlayRow;
    int overlayColor;
    int overlayAccent;

    Look set(FeatherStyle style) {
        body = style.body();
        border = style.border();
        // A style's own sprites are laid out like the sheet, so they have its size.
        ResourceLocation override = style.sprites();
        Sprites variant = FeatherVariants.variant(style.variant());
        if (variant == null) variant = FeatherVariants.variant(FeatherVariants.FEATHER);
        if (variant == null) variant = FALLBACK;
        sheet = override != null ? override : variant.texture();
        sheetWidth = override != null ? FeatherVariants.SHEET_WIDTH : variant.textureWidth();
        sheetHeight = override != null ? FeatherVariants.SHEET_HEIGHT : variant.textureHeight();
        row = variant.row();
        slotSheet = override != null ? override : FeatherVariants.SHEET;
        slotWidth = FeatherVariants.SHEET_WIDTH;
        slotHeight = FeatherVariants.SHEET_HEIGHT;
        Sprites over = style.overlay() != null ? FeatherVariants.overlay(style.overlay()) : null;
        overlay = over != null;
        if (overlay) {
            overlaySheet = override != null ? override : over.texture();
            overlayWidth = override != null ? FeatherVariants.SHEET_WIDTH : over.textureWidth();
            overlayHeight = override != null ? FeatherVariants.SHEET_HEIGHT : over.textureHeight();
            overlayRow = over.row();
            overlayColor = style.overlayColor();
            overlayAccent = style.overlayAccent();
        }
        return this;
    }

    /** Colors only, keeping the sprites: for feathers tinted from a texture (mounts, armor pieces, weight sources). */
    Look colors(int body, int border) {
        this.body = body;
        this.border = border;
        return this;
    }
}
