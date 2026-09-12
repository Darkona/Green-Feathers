package com.darkona.feathersoffatigue.compatibility.jade;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.core.FeathersServiceImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * "Stamina: 13/20 feathers" on a mount. The server sends the current value: clients only know the mount they ride.
 */
enum MountStaminaProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
    INSTANCE;

    private static final ResourceLocation UID = id("mount_stamina");
    private static final String FEATHERS = "feathers";
    private static final String MAX = "max_feathers";

    @Override
    public void appendServerData(CompoundTag data, ServerPlayer player, Level level, Entity target, boolean showDetails) {
        if (target instanceof LivingEntity entity && FeathersServiceImpl.isMount(entity)) {
            FeathersView feathers = FeathersAPI.get(entity);
            data.putInt(FEATHERS, feathers.feathers());
            data.putInt(MAX, feathers.maxFeathers());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data.contains(MAX)) {
            tooltip.add(Component.translatable("jade.feathers_of_fatigue.mount_stamina", data.getInt(FEATHERS), data.getInt(MAX)));
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
