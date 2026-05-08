package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

/**
 * The local player's feathers, client side only. What the server last synced, adjusted by local predictions.
 */
public final class ClientFeathers {

    private static ClientFeathersService service;

    private ClientFeathers() {}

    private static ClientFeathersService service() {
        return Objects.requireNonNull(service, "Green Feathers client is not loaded");
    }

    /**
     * The local player's feathers as the client knows them.
     */
    public static FeathersView local() {
        return service().local();
    }

    /**
     * The feathers of the mount the local player is riding, or {@link FeathersView#NONE} when not riding one.
     */
    public static FeathersView mount() {
        return service().mount();
    }

    /**
     * For actions decided on the client (e.g. a dodge key): checks the local feathers and, if they allow it,
     * lowers them right away so the HUD doesn't lag. The server's next sync is authoritative; pair it with a
     * server-side {@code FeathersAPI.spend}, or with {@link #requestSpend}.
     */
    public static SpendResult predictSpend(int stamina, boolean allowStrain) {
        return service().predictSpend(stamina, allowStrain);
    }

    /**
     * Asks the server to spend for the local player, for mods without their own packet. The server applies the
     * normal rules and the options' Strain and regeneration delay (not simulate or ignoreExhaustion); a cost can
     * only hurt the player who sends it.
     */
    public static void requestSpend(ResourceLocation source, int stamina, SpendOptions options) {
        service().requestSpend(source, stamina, options);
    }

    /**
     * Internal: Green Feathers installs its client implementation here.
     */
    public static void setService(ClientFeathersService implementation) {
        if (service != null) throw new IllegalStateException("The Green Feathers client service is already set");
        service = implementation;
    }
}
