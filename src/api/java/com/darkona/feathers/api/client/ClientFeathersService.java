package com.darkona.feathers.api.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Implemented by Green Feathers on the client. Use {@link ClientFeathers}.
 *
 * @hidden
 */
@ApiStatus.Internal
public interface ClientFeathersService {

    FeathersView local();

    FeathersView mount();

    SpendResult predictSpend(int stamina, boolean allowStrain);

    void requestSpend(ResourceLocation source, int stamina, SpendOptions options);
}
