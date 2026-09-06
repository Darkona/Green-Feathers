package com.darkona.feathersoffatigue.client.gui;


/**
 * The cells of a feather sheet (see FeatherVariants): 9x9, {@code u} a column times 9, {@code v} a row times 9.
 * Row 0 holds the empty slot's fill and the regeneration flash; each variant is a row of body, border and shine cells,
 * each overlay a row of primary and accent cells.
 */
final class Icons {

    private Icons() {}

    static final int SIZE = 9;

    /* Row 0. */
    static final int EMPTY_U = 0;
    /** Always from Feathers of Fatigue's sheet, untinted. */
    static final int REGEN_OVERLAY_U = 9;

    /* A variant's row. */
    static final int BODY_FULL_U = 0;
    static final int BODY_HALF_U = 9;
    static final int BORDER_FULL_U = 18;
    static final int BORDER_HALF_U = 27;
    static final int SHINE_FULL_U = 36;
    static final int SHINE_HALF_U = 45;

    /* An overlay's row. */
    static final int OVERLAY_U = 0;
    static final int OVERLAY_ACCENT_U = 9;
}
