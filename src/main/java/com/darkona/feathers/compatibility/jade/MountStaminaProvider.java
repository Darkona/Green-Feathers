package com.darkona.feathers.compatibility.jade;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.core.FeathersServiceImpl;
import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * "Stamina: 13/20 feathers" on a mount. The server sends the current value: clients only know the mount they ride.
 */
enum MountStaminaProvider implements IEntityComponentProvider, IServerDataProvider<Entity> {
    INSTANCE;

    /** Its on/off switch in Jade's plugin settings. */
    static final ResourceLocation UID = id("mount_stamina");
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
        if (config.get(UID) && data.contains(MAX)) {
            tooltip.add(new TranslatableComponent("jade.greenfeathers.mount_stamina", data.getInt(FEATHERS), data.getInt(MAX)));
        }
    }
}
