package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Picks the style of the local player's own feathers by condition: a disease, a biome, a status of your own mod.
 * Providers run from highest to lowest priority, and the first non-null answer wins. Without an answer, the HUD uses
 * the player's configured color. Built-in states use {@link FeatherStyles#STATUS_PRIORITY}. Green Feathers calls
 * providers once per client tick, so implementations should avoid expensive work.
 */
@FunctionalInterface
public interface FeatherStyleProvider {

    /**
     * Selects a registered style for the local player.
     *
     * @param player   the local player
     * @param feathers the player's feathers as the client knows them
     * @return the id of a registered style (see {@link FeatherStyles#registerStyle}), or null to let the next
     * provider decide
     */
    @Nullable
    ResourceLocation styleFor(Player player, FeathersView feathers);
}
