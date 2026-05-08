package com.darkona.feathers.compatibility.jade;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.core.FeathersServiceImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * "Stamina: 13/20 feathers" on a mount. The server sends the current value: clients only know the mount they ride.
 */
enum MountStaminaProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = id("mount_stamina");
    private static final String FEATHERS = "feathers";
    private static final String MAX = "max_feathers";

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity entity && FeathersServiceImpl.isMount(entity)) {
            FeathersView feathers = FeathersAPI.get(entity);
            data.putInt(FEATHERS, feathers.feathers());
            data.putInt(MAX, feathers.maxFeathers());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(MAX)) {
            tooltip.add(Component.translatable("jade.greenfeathers.mount_stamina", data.getInt(FEATHERS), data.getInt(MAX)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
