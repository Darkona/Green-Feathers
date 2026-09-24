package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.SpendOptions;
import com.darkona.feathersoffatigue.api.SpendResult;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implemented by Feathers of Fatigue on the client. Use {@link ClientFeathers}.
 *
 * @hidden
 */
@ApiStatus.Internal
public interface ClientFeathersService {

    FeathersView local();

    FeathersView mount();

    SpendResult predictSpend(Identifier source, int stamina, SpendOptions options);

    void requestSpend(Identifier source, int stamina, SpendOptions options);
}
