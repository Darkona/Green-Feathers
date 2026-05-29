package com.darkona.feathers.compatibility.jade;

import net.minecraft.world.entity.LivingEntity;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

/**
 * Jade: looking at a mount shows its stamina. Jade finds and loads this class itself, only when installed.
 */
@WailaPlugin
public class FeathersJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.addConfig(MountStaminaProvider.UID, true);
        registration.registerEntityDataProvider(MountStaminaProvider.INSTANCE, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerComponentProvider(MountStaminaProvider.INSTANCE, TooltipPosition.BODY, LivingEntity.class);
    }
}
