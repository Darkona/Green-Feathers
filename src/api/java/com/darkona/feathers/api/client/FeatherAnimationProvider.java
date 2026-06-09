package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/**
 * Animates the feather row by condition. Providers are asked in order of priority, highest first; the first non-null
 * answer wins, and with none the row stays still. Green Feathers' own triggers (strain, low feathers, energized, each
 * set in the client config) answer at {@link FeatherAnimations#STATUS_PRIORITY}. Asked once per client tick, on the
 * client; keep it cheap.
 */
@FunctionalInterface
public interface FeatherAnimationProvider {

    /**
     * @param entity   whose feathers the row shows: the local player, or the mount they ride
     * @param feathers those feathers, as the client knows them
     * @return the animation, or null to let the next provider decide
     */
    @Nullable
    FeatherAnimation animationFor(LivingEntity entity, FeathersView feathers);
}
