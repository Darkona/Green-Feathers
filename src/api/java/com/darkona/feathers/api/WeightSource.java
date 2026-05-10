package com.darkona.feathers.api;

import com.darkona.feathers.api.event.ArmorWeightEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

/**
 * Extra weight, in feathers, on top of worn armor: a backpack, a heavy accessory, a full inventory. Added before
 * {@link ArmorWeightEvent} and the armor weight multiplier. Evaluated whenever the weight is recalculated, server side.
 * <p>
 * The HUD draws armor weight piece by piece, head to feet, each in the piece's color. A source that gives a
 * {@link #color} or a {@link #displayItem} gets its own share after the boots, in that color (or the item's dominant
 * color); one that gives neither is drawn grey with the rest of the unattributed weight.
 */
@FunctionalInterface
public interface WeightSource {

    /** No color of its own: drawn grey. */
    int NO_COLOR = -1;

    double weight(LivingEntity entity);

    /**
     * The color its weight is drawn in, {@code 0xRRGGBB}, or {@link #NO_COLOR}. Wins over {@link #displayItem}.
     */
    default int color(LivingEntity entity) {
        return NO_COLOR;
    }

    /**
     * The item its weight is drawn after (a backpack): the feathers take the item's dominant color, or its dye for
     * dyeable items. Null for none.
     */
    default @Nullable Item displayItem(LivingEntity entity) {
        return null;
    }
}
