package com.darkona.feathersoffatigue.compatibility.jade;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * "Stamina: 13/20 feathers" on a mount, from what {@link MountStaminaData} sent: clients only know the mount they ride.
 */
enum MountStaminaProvider implements IEntityComponentProvider {
    INSTANCE;

    static final Identifier UID = id("mount_stamina");
    static final String FEATHERS = "feathers";
    static final String MAX = "max_feathers";

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(MAX)) {
            tooltip.add(Component.translatable("jade.feathers_of_fatigue.mount_stamina", data.getIntOr(FEATHERS, 0), data.getIntOr(MAX, 0)));
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
