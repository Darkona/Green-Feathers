package com.darkona.feathersoffatigue.compatibility.jade;

import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade: looking at a mount shows its stamina. Jade finds and loads this class itself, only when installed.
 */
@WailaPlugin
public class FeathersJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerEntityDataProvider(MountStaminaData.INSTANCE, LivingEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerEntityComponent(MountStaminaProvider.INSTANCE, LivingEntity.class);
    }
}
