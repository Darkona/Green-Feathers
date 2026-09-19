package com.darkona.feathersoffatigue.compatibility.dropletsofthirst;

import com.darkona.dropletsofthirst.api.DropletsAPI;
import com.darkona.dropletsofthirst.api.DropletsView;
import net.minecraft.world.entity.player.Player;

/**
 * The only class that touches Droplets of Thirst, through its public API. Loaded only through {@link DropletsOfThirstCompat}
 * when the mod is present.
 */
final class DropletsOfThirstBridge {

    private DropletsOfThirstBridge() {}

    /** Live view, no copy: read it within the call. */
    static DropletsView view(Player player) {
        return DropletsAPI.view(player);
    }

    static void drain(Player player, int points) {
        DropletsAPI.addThirst(player, -points, 0);
    }
}
