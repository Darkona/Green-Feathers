package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.SpendOptions;
import com.darkona.feathersoffatigue.api.SpendResult;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * The local player's feathers, client side only. What the server last synced, adjusted by local predictions.
 */
public final class ClientFeathers {

    private static ClientFeathersService service;

    private ClientFeathers() {}

    private static ClientFeathersService service() {
        return Objects.requireNonNull(service, "Feathers of Fatigue client is not loaded");
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
     * lowers them immediately to keep the HUD responsive. The cost goes through the usage multiplier and the stamina
     * modifiers, as on the server. The server's next sync is authoritative. Pair this call with a server-side
     * {@code FeathersAPI.spend} with the same source and cost, or use {@link #requestSpend}.
     *
     * @param source  a stable identifier for the action, the one the server-side spend uses
     * @param stamina the base cost in stamina units
     * @param options the spend options; {@link SpendOptions#simulate()} only checks
     * @return the predicted outcome
     */
    public static SpendResult predictSpend(Identifier source, int stamina, SpendOptions options) {
        return service().predictSpend(source, stamina, options);
    }

    /**
     * Asks the server to spend for the local player, for mods without their own packet. The server applies the
     * normal rules and honors the strain and regeneration delay options. It ignores simulation and exhaustion
     * overrides for safety. The request can affect only the player who sent it.
     *
     * @param source  a stable identifier for the action
     * @param stamina the base cost in stamina units
     * @param options the server-side spend options
     */
    public static void requestSpend(Identifier source, int stamina, SpendOptions options) {
        service().requestSpend(source, stamina, options);
    }

    /**
     * Installs the internal client service. Feathers of Fatigue calls this once during client startup.
     *
     * @param implementation the client service implementation
     * @throws NullPointerException if {@code implementation} is {@code null}
     * @throws IllegalStateException if a service is already installed
     */
    @ApiStatus.Internal
    public static void setService(ClientFeathersService implementation) {
        if (service != null) throw new IllegalStateException("The Feathers of Fatigue client service is already set");
        service = Objects.requireNonNull(implementation, "implementation");
    }
}
