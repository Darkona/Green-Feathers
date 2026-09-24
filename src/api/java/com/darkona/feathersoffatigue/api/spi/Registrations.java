package com.darkona.feathersoffatigue.api.spi;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Arrays;
import java.util.function.Function;

/**
 * The arrays behind Feathers of Fatigue's registries (climate providers, regeneration factors, style and animation
 * providers): replaced wholesale on registration, read every tick or frame without locks or iterators. This internal
 * class can change between versions.
 *
 * @hidden
 */
@ApiStatus.Internal
public final class Registrations {

    private Registrations() {}

    /**
     * A copy of {@code array} with {@code entry} added, replacing any entry with the same id in place.
     *
     * @param array the current entries
     * @param entry the entry to add
     * @param id    reads an entry's id
     * @param <T>   the entry type
     * @return a new array
     */
    public static <T> T[] withEntry(T[] array, T entry, Function<T, Identifier> id) {
        Identifier key = id.apply(entry);
        for (int i = 0; i < array.length; i++) {
            if (id.apply(array[i]).equals(key)) {
                T[] copy = array.clone();
                copy[i] = entry;
                return copy;
            }
        }
        T[] copy = Arrays.copyOf(array, array.length + 1);
        copy[array.length] = entry;
        return copy;
    }
}
