package com.darkona.feathers.compatibility.bluedroplets;

import com.darkona.droplets.api.DropletsAPI;
import com.darkona.droplets.api.DropletsView;
import net.minecraft.world.entity.player.Player;

/**
 * The only class that touches Blue Droplets, through its public API. Loaded only through {@link BlueDropletsCompat}
 * when the mod is present.
 */
final class BlueDropletsBridge {

    private BlueDropletsBridge() {}

    /** Live view, no copy: read it within the call. */
    static DropletsView view(Player player) {
        return DropletsAPI.view(player);
    }

    static void drain(Player player, int points) {
        DropletsAPI.addThirst(player, -points, 0);
    }
}
