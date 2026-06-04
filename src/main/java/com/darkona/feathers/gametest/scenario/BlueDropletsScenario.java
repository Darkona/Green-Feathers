package com.darkona.feathers.gametest.scenario;

import com.darkona.droplets.api.DropletsAPI;
import net.minecraft.world.entity.player.Player;

/**
 * Sets Blue Droplets state for tests through its API. Only loaded when it is present.
 */
public final class BlueDropletsScenario {

    private BlueDropletsScenario() {}

    public static void thirst(Player player, int thirst, int quenched) {
        DropletsAPI.setThirst(player, thirst);
        DropletsAPI.setQuenched(player, quenched);
    }

    public static int thirst(Player player) {
        return DropletsAPI.view(player).thirst();
    }
}
