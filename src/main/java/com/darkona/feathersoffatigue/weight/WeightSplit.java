package com.darkona.feathersoffatigue.weight;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import java.util.Arrays;

/**
 * How an entity's weight divides up, in whole feathers adding up to the total: each armor piece (head to feet), each
 * weight source with a color of its own, and the rest. Reused for every recalculation: nothing is allocated once the
 * lists have grown to the number of sources.
 */
public final class WeightSplit {

    /** Feathers per armor piece ({@link ArmorWeights#HEAD} to {@link ArmorWeights#FEET}) and {@link ArmorWeights#OTHER}. */
    final int[] parts = new int[ArmorWeights.PARTS];
    /** Colored sources, in pairs: tint (see {@link #tint}), feathers. */
    final IntArrayList sources = new IntArrayList();
    /** Scratch for the unrounded shares: the parts, then one per colored source. */
    final DoubleArrayList shares = new DoubleArrayList();

    public int part(int part) {
        return parts[part];
    }

    public int sourceCount() {
        return sources.size() / 2;
    }

    /**
     * A colored source's tint: {@code 0xRRGGBB} when it is a color, or {@code -(item id + 1)} when it is an item whose
     * colors to use.
     */
    public int tint(int source) {
        return sources.getInt(2 * source);
    }

    public int feathers(int source) {
        return sources.getInt(2 * source + 1);
    }

    public void clear() {
        Arrays.fill(parts, 0);
        sources.clear();
        shares.clear();
    }

    /** Flattened for the network: the parts, then the source pairs. A copy. */
    public int[] toArray() {
        int[] out = new int[ArmorWeights.PARTS + sources.size()];
        System.arraycopy(parts, 0, out, 0, ArmorWeights.PARTS);
        sources.getElements(0, out, ArmorWeights.PARTS, sources.size());
        return out;
    }

    /** From {@link #toArray}. */
    public void readFrom(int[] flat) {
        clear();
        System.arraycopy(flat, 0, parts, 0, ArmorWeights.PARTS);
        sources.addElements(0, flat, ArmorWeights.PARTS, flat.length - ArmorWeights.PARTS);
    }

    public boolean sameAs(WeightSplit other) {
        return Arrays.equals(parts, other.parts) && sources.equals(other.sources);
    }

    public void copyFrom(WeightSplit other) {
        System.arraycopy(other.parts, 0, parts, 0, ArmorWeights.PARTS);
        sources.clear();
        sources.addAll(other.sources);
    }
}
