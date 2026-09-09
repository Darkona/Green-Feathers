package com.darkona.feathersoffatigue.api.client;

import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.api.SpendOptions;
import com.darkona.feathersoffatigue.api.SpendResult;
import net.minecraft.resources.ResourceLocation;
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

    SpendResult predictSpend(ResourceLocation source, int stamina, SpendOptions options);

    void requestSpend(ResourceLocation source, int stamina, SpendOptions options);
}
