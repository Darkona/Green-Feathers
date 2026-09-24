package com.darkona.feathersoffatigue.compatibility.jade;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.core.FeathersServiceImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * The server side of {@link MountStaminaProvider}: a mount's current feathers. Jade wants the data and the tooltip in
 * separate providers, with the same id.
 */
enum MountStaminaData implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag data, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof LivingEntity entity && FeathersServiceImpl.isMount(entity)) {
            FeathersView feathers = FeathersAPI.get(entity);
            data.putInt(MountStaminaProvider.FEATHERS, feathers.feathers());
            data.putInt(MountStaminaProvider.MAX, feathers.maxFeathers());
        }
    }

    @Override
    public Identifier getUid() {
        return MountStaminaProvider.UID;
    }
}
