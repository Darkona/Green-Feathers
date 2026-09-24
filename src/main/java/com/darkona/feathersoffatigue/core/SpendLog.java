package com.darkona.feathersoffatigue.core;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import net.minecraft.resources.Identifier;

/**
 * Stores recent spend activity by source for {@code /feathers debug}. A fixed ring combines spends from the same
 * source within one second, which prevents drains from flooding the log. The first spend creates the log.
 */
public final class SpendLog {

    private static final int SIZE = 32;
    private static final int FOLD_TICKS = 20;

    private final Identifier[] sources = new Identifier[SIZE];
    private final int[] amounts = new int[SIZE];
    private final long[] times = new long[SIZE];
    private int head = -1;
    private int count;

    void record(Identifier source, int amount, long gameTime) {
        if (head >= 0 && source.equals(sources[head]) && gameTime - times[head] < FOLD_TICKS) {
            amounts[head] += amount;
            times[head] = gameTime;
            return;
        }
        head = (head + 1) % SIZE;
        sources[head] = source;
        amounts[head] = amount;
        times[head] = gameTime;
        if (count < SIZE) count++;
    }

    /**
     * Stamina spent per source since {@code sinceGameTime}, most recent source first.
     */
    public Object2IntLinkedOpenHashMap<Identifier> totalsSince(long sinceGameTime) {
        Object2IntLinkedOpenHashMap<Identifier> totals = new Object2IntLinkedOpenHashMap<>();
        for (int i = 0; i < count; i++) {
            int index = Math.floorMod(head - i, SIZE);
            if (times[index] < sinceGameTime) break;
            totals.addTo(sources[index], amounts[index]);
        }
        return totals;
    }
}
