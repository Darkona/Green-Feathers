package com.darkona.feathers.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.client.ClientFeathersService;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.network.SpendDebugPayload;
import com.darkona.feathers.network.SpendRequestPayload;
import com.darkona.feathers.network.SyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

/**
 * The local player's feathers (this view) and those of the mount it rides, as last synced, with local predictions
 * for the player until the next sync. Client thread only.
 */
public final class ClientFeathersData extends SyncedFeathers implements ClientFeathersService, FeathersServiceImpl.ClientBridge {

    public static final ClientFeathersData INSTANCE = new ClientFeathersData();

    /** How long a debug spend line stays on the overlay. */
    public static final int DEBUG_LINE_TICKS = 60;

    private final SyncedFeathers mount = new SyncedFeathers();
    private int mountId = -1;

    private ResourceLocation lastSpendSource;
    private int lastSpendCost;
    private int lastSpendTicks;

    private ClientFeathersData() {}

    public static void accept(SyncPayload payload) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && payload.entityId() != player.getId()) {
            INSTANCE.mountId = payload.entityId();
            INSTANCE.mount.apply(payload);
        } else {
            INSTANCE.apply(payload);
        }
    }

    public static void acceptDebug(SpendDebugPayload payload) {
        INSTANCE.lastSpendSource = payload.source();
        INSTANCE.lastSpendCost = payload.cost();
        INSTANCE.lastSpendTicks = DEBUG_LINE_TICKS;
    }

    /**
     * Forgets everything on leaving a world.
     */
    @Override
    void clear() {
        super.clear();
        mount.clear();
        mountId = -1;
        lastSpendSource = null;
    }

    @Override
    void tick() {
        super.tick();
        mount.tick();
        if (lastSpendTicks > 0) lastSpendTicks--;
        // The mount's feathers only mean something while riding it.
        LocalPlayer player = Minecraft.getInstance().player;
        Entity vehicle = player != null ? player.getVehicle() : null;
        if (mountId != -1 && (vehicle == null || vehicle.getId() != mountId)) {
            mount.clear();
            mountId = -1;
        }
    }

    /**
     * The feathers of the mount the local player rides, or {@link FeathersView#NONE}.
     */
    public FeathersView mount() {
        return mount.hasFeathers() ? mount : FeathersView.NONE;
    }

    public ResourceLocation lastSpendSource() {
        return lastSpendTicks > 0 ? lastSpendSource : null;
    }

    public int lastSpendCost() {
        return lastSpendCost;
    }

    /* ClientFeathersService, and the common service's client bridge */

    @Override
    public FeathersView local() {
        return this;
    }

    @Override
    public @Nullable FeathersView clientView(LivingEntity entity) {
        if (entity == Minecraft.getInstance().player) return this;
        return entity.getId() == mountId && mount.hasFeathers() ? mount : null;
    }

    @Override
    public boolean isLocalPlayer(LivingEntity entity) {
        return entity == Minecraft.getInstance().player;
    }

    /**
     * Pays locally the way the server would (bonus, stamina, then Strain) so the HUD reacts at once.
     */
    @Override
    public SpendResult predictSpend(int cost, boolean allowStrain) {
        if (!synced) return SpendResult.EXEMPT;
        if (exhausted) return SpendResult.EXHAUSTED;
        int strainRoom = allowStrain && FeathersCommonConfig.ENABLE_STRAIN.get() ? Math.max(0, maxStrain - strain) : 0;
        if (cost > availableStamina() + strainRoom) return SpendResult.INSUFFICIENT;

        int fromBonus = Math.min(bonus, cost);
        bonus -= fromBonus;
        int left = cost - fromBonus;
        int fromStamina = Math.min(Math.max(0, stamina - Stamina.ofFeathers(weight)), left);
        stamina -= fromStamina;
        strain += left - fromStamina;
        regenDelay = Math.max(regenDelay, FeathersCommonConfig.DEFAULT_USAGE_COOLDOWN.get());
        return SpendResult.OK;
    }

    @Override
    public void requestSpend(ResourceLocation source, int stamina, SpendOptions options) {
        PacketDistributor.sendToServer(new SpendRequestPayload(source, stamina, options.allowStrain(), options.regenDelayTicks()));
    }

    @Override
    public FeathersView localView() {
        return this;
    }
}
