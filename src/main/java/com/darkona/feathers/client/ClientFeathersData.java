package com.darkona.feathers.client;

import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.RestState;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * The local player's feathers: the server's last snapshot, adjusted by local predictions until the next one.
 * Client thread only.
 */
public final class ClientFeathersData implements FeathersView, ClientFeathersService, FeathersServiceImpl.ClientBridge {

    public static final ClientFeathersData INSTANCE = new ClientFeathersData();

    /** How long a debug spend line stays on the overlay. */
    public static final int DEBUG_LINE_TICKS = 60;

    private boolean synced;
    private int stamina, maxStamina, strain, maxStrain, bonus, weight, regenDelay;
    private boolean exhausted;
    private RestState rest = RestState.NONE;

    private ResourceLocation lastSpendSource;
    private int lastSpendCost;
    private int lastSpendTicks;

    private ClientFeathersData() {}

    public static void accept(SyncPayload payload) {
        INSTANCE.apply(payload);
    }

    public static void acceptDebug(SpendDebugPayload payload) {
        INSTANCE.lastSpendSource = payload.source();
        INSTANCE.lastSpendCost = payload.cost();
        INSTANCE.lastSpendTicks = DEBUG_LINE_TICKS;
    }

    private void apply(SyncPayload p) {
        synced = true;
        stamina = p.stamina();
        maxStamina = p.maxStamina();
        strain = p.strain();
        maxStrain = p.maxStrain();
        bonus = p.bonus();
        weight = p.weight();
        regenDelay = p.regenDelay();
        exhausted = p.exhausted();
        rest = p.rest();
    }

    /**
     * Forgets everything on leaving a world.
     */
    void clear() {
        synced = false;
        stamina = maxStamina = strain = maxStrain = bonus = weight = regenDelay = 0;
        exhausted = false;
        rest = RestState.NONE;
        lastSpendSource = null;
    }

    void tick() {
        if (regenDelay > 0) regenDelay--;
        if (lastSpendTicks > 0) lastSpendTicks--;
    }

    public ResourceLocation lastSpendSource() {
        return lastSpendTicks > 0 ? lastSpendSource : null;
    }

    public int lastSpendCost() {
        return lastSpendCost;
    }

    /* FeathersView */

    @Override
    public boolean hasFeathers() {
        return synced;
    }

    @Override
    public int stamina() {
        return stamina;
    }

    @Override
    public int maxStamina() {
        return maxStamina;
    }

    @Override
    public int availableStamina() {
        return Math.max(0, stamina - Stamina.ofFeathers(weight)) + bonus;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public int strain() {
        return strain;
    }

    @Override
    public int maxStrain() {
        return maxStrain;
    }

    @Override
    public int bonusStamina() {
        return bonus;
    }

    @Override
    public int regenDelay() {
        return regenDelay;
    }

    @Override
    public boolean exhausted() {
        return exhausted;
    }

    @Override
    public RestState restState() {
        return rest;
    }

    /* ClientFeathersService, and the common service's client bridge */

    @Override
    public FeathersView local() {
        return this;
    }

    @Override
    public boolean isLocalPlayer(LivingEntity entity) {
        return entity == Minecraft.getInstance().player;
    }

    @Override
    public FeathersView localView() {
        return this;
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
}
