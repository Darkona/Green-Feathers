package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Picks the style of the local player's own feathers by condition: a disease, a biome, a status of your own mod.
 * Providers are asked in order of priority, highest first; the first non-null answer wins, and with none the player's
 * configured color is used. Green Feathers' own states (cold, hot, energized, momentum) answer at
 * {@link FeatherStyles#STATUS_PRIORITY}. Asked once per client tick, on the client; keep it cheap.
 */
@FunctionalInterface
public interface FeatherStyleProvider {

    /**
     * @param feathers the player's feathers as the client knows them
     * @return the id of a registered style (see {@link FeatherStyles#registerStyle}), or null to let the next
     * provider decide
     */
    @Nullable
    ResourceLocation styleFor(Player player, FeathersView feathers);
}
