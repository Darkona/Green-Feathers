package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

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
     * Gets the local player's latest synchronized and predicted stamina state. Use this for client-only UI or input.
     *
     * @return the local player's live client view
     */
    public static FeathersView local() {
        return service().local();
    }

    /**
     * Gets the stamina of the local player's current mount.
     *
     * @return the mount's live client view, or {@link FeathersView#NONE} when no supported mount is active
     */
    public static FeathersView mount() {
        return service().mount();
    }

    /**
     * For actions decided on the client (e.g. a dodge key): checks the local feathers and, if they allow it,
     * lowers them immediately to keep the HUD responsive. The server's next sync is authoritative. Pair this call
     * with a server-side {@code FeathersAPI.spend}, or use {@link #requestSpend}.
     *
     * @param stamina    the predicted cost in stamina units
     * @param allowStrain whether the prediction may create more Strain
     * @return the predicted outcome
     */
    public static SpendResult predictSpend(int stamina, boolean allowStrain) {
        return service().predictSpend(stamina, allowStrain);
    }

    /**
     * Asks the server to spend for the local player, for mods without their own packet. The server applies the
     * normal rules and honors the Strain and regeneration delay options. It ignores simulation and exhaustion
     * overrides for safety. The request can affect only the player who sent it.
     *
     * @param source  a stable identifier for the action
     * @param stamina the base cost in stamina units
     * @param options the server-side spend options
     */
    public static void requestSpend(ResourceLocation source, int stamina, SpendOptions options) {
        service().requestSpend(source, stamina, options);
    }

    /**
     * Installs the internal client service. Green Feathers calls this once during client startup.
     *
     * @param implementation the client service implementation
     * @throws NullPointerException if {@code implementation} is {@code null}
     * @throws IllegalStateException if a service is already installed
     */
    @ApiStatus.Internal
    public static void setService(ClientFeathersService implementation) {
        if (service != null) throw new IllegalStateException("The Green Feathers client service is already set");
        service = Objects.requireNonNull(implementation, "implementation");
    }
}
