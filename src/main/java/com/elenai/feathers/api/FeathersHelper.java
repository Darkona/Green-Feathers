package com.elenai.feathers.api;

import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.client.ClientFeathers;
import com.darkona.feathers.config.FeathersServerConfig;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * The client-side API of Elenai's original Feathers, kept for mods written against it. New code: use
 * {@code FeathersAPI} on the server or {@code ClientFeathers} on the client.
 */
@Deprecated
public final class FeathersHelper {

    private FeathersHelper() {}

    @Deprecated
    public static boolean spendFeathers(int amount) {
        return spendFeathers(amount, FeathersServerConfig.DEFAULT_USAGE_COOLDOWN.get());
    }

    /**
     * Spends whole feathers for the local player: predicted on the client, then asked of the server.
     *
     * @param cooldown ticks without regeneration afterwards
     */
    @Deprecated
    public static boolean spendFeathers(int amount, int cooldown) {
        int stamina = Stamina.ofFeathers(amount);
        SpendResult result = ClientFeathers.predictSpend(stamina, true);
        if (result == SpendResult.OK) ClientFeathers.requestSpend(id("legacy_feathers"), stamina, SpendOptions.DEFAULT.withRegenDelay(cooldown));
        return result.allowed();
    }
}
