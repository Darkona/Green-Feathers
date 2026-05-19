package com.darkona.feathers.gametest.scenario;

import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;

/**
 * Sets Thirst Was Taken state for tests. Only loaded when it is present.
 */
public final class ThirstScenario {

    private ThirstScenario() {}

    public static void thirst(Player player, int thirst, int quench) {
        player.getCapability(ModCapabilities.PLAYER_THIRST).ifPresent(data -> {
            data.setThirst(thirst);
            data.setQuenched(quench);
        });
    }
}
