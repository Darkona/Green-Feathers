package com.darkona.feathers.client.gui;


/**
 * Where each feather icon sits in icons.png: one column per color, rows background, half, full.
 */
final class Icons {

    private Icons() {}

    static final Set NORMAL = new Set(0x22A5F0, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(0, 1),
            GuiIcon.featherIcon(0, 2));
    static final Set COLD = new Set(0x9BE7F2, GuiIcon.featherIcon(1, 0),
            GuiIcon.featherIcon(1, 1),
            GuiIcon.featherIcon(1, 2));
    static final Set HOT = new Set(0xFF870C, GuiIcon.featherIcon(2, 0),
            GuiIcon.featherIcon(2, 1),
            GuiIcon.featherIcon(2, 2));
    static final Set GREEN = new Set(0x00B53A, GuiIcon.featherIcon(3, 0),
            GuiIcon.featherIcon(3, 1),
            GuiIcon.featherIcon(3, 2));
    static final Set ENERGY = new Set(0xFBEE2B, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(5, 1),
            GuiIcon.featherIcon(5, 2));
    static final Set MOMENTUM = new Set(0x5CC9A7, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(6, 1),
            GuiIcon.featherIcon(6, 2));
    static final Set STRAINED = new Set(0x940000, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(9, 1),
            GuiIcon.featherIcon(9, 2));
    static final Set ENDURANCE = new Set(0xD4AF37, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(10, 1),
            GuiIcon.featherIcon(10, 2));
    static final Set ARMOR = new Set(0x8A8A8A, GuiIcon.featherIcon(0, 0),
            GuiIcon.featherIcon(3, 4),
            GuiIcon.featherIcon(3, 3));
    /** Grey feather bodies to tint with a color, and white outlines to tint with its complement. */
    static final GuiIcon TINT_BODY_HALF = GuiIcon.featherIcon(12, 1);
    static final GuiIcon TINT_BODY_FULL = GuiIcon.featherIcon(12, 2);
    static final GuiIcon TINT_EDGE_HALF = GuiIcon.featherIcon(13, 1);
    static final GuiIcon TINT_EDGE_FULL = GuiIcon.featherIcon(13, 2);
    static final GuiIcon REGEN_OVERLAY = GuiIcon.featherIcon(0, 3);

    /** {@code color}: the feathers' main color, which rows layered over them are shaded from. */
    record Set(int color, GuiIcon background, GuiIcon half, GuiIcon full) {
    }
}
