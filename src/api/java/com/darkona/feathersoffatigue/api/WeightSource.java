package com.darkona.feathersoffatigue.api;

import com.darkona.feathersoffatigue.api.event.ArmorWeightEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Extra weight, in feathers, on top of worn armor: a backpack, a heavy accessory, a full inventory. Added before
 * {@link ArmorWeightEvent} and the armor weight multiplier. Feathers of Fatigue evaluates each source when it recalculates
 * weight on the server.
 * <p>
 * The HUD draws armor weight piece by piece, head to feet, each in the piece's color. A source that gives a
 * {@link #color} or a {@link #displayItem} gets its own share after the boots, in that color (or the item's dominant
 * color). A source that gives neither is drawn gray with the rest of the unattributed weight.
 */
@FunctionalInterface
public interface WeightSource {

    /** Indicates that the source has no display color. The HUD draws its weight in gray. */
    int NO_COLOR = -1;

    /**
     * Calculates the source's current extra weight.
     *
     * @param entity the entity whose equipment or state to inspect
     * @return the extra weight in feathers
     */
    double weight(LivingEntity entity);

    /**
     * The color its weight is drawn in, {@code 0xRRGGBB}, or {@link #NO_COLOR}. Wins over {@link #displayItem}.
     *
     * @param entity the entity whose weight is displayed
     * @return the RGB color, or {@link #NO_COLOR}
     */
    default int color(LivingEntity entity) {
        return NO_COLOR;
    }

    /**
     * The item its weight is drawn after (a backpack): the feathers take the item's dominant color, or its dye for
     * dyeable items. Null for none.
     *
     * @param entity the entity whose weight is displayed
     * @return the representative item, or {@code null}
     */
    default @Nullable Item displayItem(LivingEntity entity) {
        return null;
    }
}
