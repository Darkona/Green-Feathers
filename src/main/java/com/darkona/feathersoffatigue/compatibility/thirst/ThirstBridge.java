package com.darkona.feathersoffatigue.compatibility.thirst;

import dev.ghen.thirst.foundation.common.capability.IThirst;
import dev.ghen.thirst.foundation.common.capability.ModCapabilities;
import net.minecraft.world.entity.player.Player;

/**
 * The only class that touches Thirst Was Taken. Loaded only through {@link ThirstCompat} when the mod is present.
 */
final class ThirstBridge {

    private ThirstBridge() {}

    static int thirst(Player player) {
        IThirst thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        return thirst != null ? thirst.getThirst() : 20;
    }

    static int quench(Player player) {
        IThirst thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        return thirst != null ? thirst.getQuenched() : 0;
    }

    static void drain(Player player, int points) {
        IThirst thirst = player.getCapability(ModCapabilities.PLAYER_THIRST).orElse(null);
        if (thirst != null) thirst.setThirst(Math.max(0, thirst.getThirst() - points));
    }
}
