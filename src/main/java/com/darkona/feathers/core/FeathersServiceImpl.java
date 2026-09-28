package com.darkona.feathers.core;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.Climate;
import com.darkona.feathers.api.ClimateProvider;
import com.darkona.feathers.api.DrainOptions;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.RegenFactor;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.StaminaModifier;
import com.darkona.feathers.api.WeightSource;
import com.darkona.feathers.api.event.DrainEvent;
import com.darkona.feathers.api.event.ExhaustionEvent;
import com.darkona.feathers.api.event.GainEvent;
import com.darkona.feathers.api.event.SpendEvent;
import com.darkona.feathers.api.event.StrainEvent;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.spi.FeathersService;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.network.FeathersNetwork;
import com.darkona.feathers.weight.ArmorWeights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

/**
 * The server-side implementation behind the API. Client-side calls go to {@link ClientBridge}.
 */
public final class FeathersServiceImpl implements FeathersService {

    public static final FeathersServiceImpl INSTANCE = new FeathersServiceImpl();

    /**
     * What client-side calls use: the local player's synced feathers. Installed by the client; on a dedicated
     * server nothing asks.
     */
    public interface ClientBridge {
        boolean isLocalPlayer(LivingEntity entity);

        FeathersView localView();

        SpendResult predictSpend(int stamina, boolean allowStrain);
    }

    private static volatile ClientBridge clientBridge;

    private FeathersServiceImpl() {}

    public static void setClientBridge(ClientBridge bridge) {
        clientBridge = bridge;
    }

    /* Access */

    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player;
    }

    /**
     * Players in creative or spectator mode don't use feathers.
     */
    public static boolean isExempt(LivingEntity entity) {
        return entity instanceof Player player && (player.isCreative() || player.isSpectator());
    }

    public static FeathersData data(LivingEntity entity) {
        return entity.getData(FeathersAttachments.FEATHERS);
    }

    private static boolean onClient(LivingEntity entity) {
        return entity.level().isClientSide();
    }

    @Override
    public FeathersView view(LivingEntity entity) {
        if (!supports(entity)) return FeathersView.NONE;
        if (onClient(entity)) {
            // The client only knows its own player's feathers.
            ClientBridge bridge = clientBridge;
            return bridge != null && bridge.isLocalPlayer(entity) ? bridge.localView() : FeathersView.NONE;
        }
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        return data;
    }

    /**
     * Max stamina and weight come from attributes and equipment; the first access after joining or loading reads
     * them, and a brand new entity starts full.
     */
    static void ensureInitialized(LivingEntity entity, FeathersData data) {
        if (data.initialized) return;
        data.initialized = true;
        refreshMaximums(entity, data);
        if (data.fresh) {
            data.stamina = data.maxStamina;
            data.fresh = false;
        }
        data.weight = ArmorWeights.totalWeight(entity);
        data.forceSync = true;
    }

    /**
     * Reads the max feathers and max Strain attributes.
     *
     * @return whether either changed
     */
    static boolean refreshMaximums(LivingEntity entity, FeathersData data) {
        AttributeInstance maxFeathers = entity.getAttribute(FeathersAttributes.MAX_FEATHERS);
        AttributeInstance maxStrain = entity.getAttribute(FeathersAttributes.MAX_STRAIN);
        int max = maxFeathers != null ? Stamina.ofFeathers(maxFeathers.getValue()) : 0;
        int strainMax = maxStrain != null ? Stamina.ofFeathers(maxStrain.getValue()) : 0;
        if (max == data.maxStamina && strainMax == data.maxStrain) return false;
        data.maxStamina = max;
        data.maxStrain = strainMax;
        if (data.stamina > max) data.stamina = max;
        return true;
    }

    /* Costs */

    /**
     * A base cost after the usage multiplier and the stamina modifiers.
     */
    static int effectiveCost(LivingEntity entity, FeathersData data, ResourceLocation source, double baseCost) {
        AttributeInstance usage = entity.getAttribute(FeathersAttributes.USAGE_MULTIPLIER);
        int cost = (int) Math.round(baseCost * (usage != null ? usage.getValue() : 1.0));
        for (Extensions.ModifierEntry modifier : Extensions.modifiers()) {
            cost = modifier.modifier().modifyCost(entity, data, source, cost);
        }
        return Math.max(0, cost);
    }

    @Override
    public SpendResult spend(LivingEntity entity, ResourceLocation source, int stamina, SpendOptions options) {
        if (!supports(entity) || isExempt(entity)) return SpendResult.EXEMPT;

        if (onClient(entity)) {
            ClientBridge bridge = clientBridge;
            if (bridge == null || !bridge.isLocalPlayer(entity)) return SpendResult.EXEMPT;
            if (options.simulate()) return simulateAgainst(bridge.localView(), stamina, options);
            return bridge.predictSpend(stamina, options.allowStrain());
        }

        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        boolean strainEnabled = FeathersCommonConfig.ENABLE_STRAIN.get();

        if (data.exhausted && !options.ignoreExhaustion()) return SpendResult.EXHAUSTED;

        // A free action still pauses regeneration if it asks to.
        if (stamina <= 0) {
            if (!options.simulate()) applyRegenDelay(data, options);
            return SpendResult.OK;
        }

        int cost = effectiveCost(entity, data, source, stamina);
        if (options.simulate()) {
            return data.canPay(cost, options.allowStrain(), strainEnabled) ? SpendResult.OK : SpendResult.INSUFFICIENT;
        }

        SpendEvent.Pre pre = NeoForge.EVENT_BUS.post(new SpendEvent.Pre(entity, source, cost, options));
        if (pre.isCanceled()) return SpendResult.CANCELLED;
        cost = pre.getCost();

        if (!data.canPay(cost, options.allowStrain(), strainEnabled)) return SpendResult.INSUFFICIENT;

        payAndSettle(entity, data, cost, strainEnabled);

        applyRegenDelay(data, options);

        NeoForge.EVENT_BUS.post(new SpendEvent.Post(entity, source, cost, SpendResult.OK));
        if (FeathersCommonConfig.DEBUG_MODE.get()) {
            Feathers.LOGGER.info("{} spent {} stamina on {}", entity.getName().getString(), cost, source);
            FeathersNetwork.sendSpendDebug(entity, source, cost);
        }
        return SpendResult.OK;
    }

    private static void applyRegenDelay(FeathersData data, SpendOptions options) {
        int delay = options.regenDelayTicks() < 0 ? FeathersCommonConfig.DEFAULT_USAGE_COOLDOWN.get() : options.regenDelayTicks();
        data.regenDelay = Math.min(data.regenDelay + delay, FeathersCommonConfig.MAX_COOLDOWN.get() * 20);
    }

    private static SpendResult simulateAgainst(FeathersView view, int cost, SpendOptions options) {
        if (view.exhausted() && !options.ignoreExhaustion()) return SpendResult.EXHAUSTED;
        int room = options.allowStrain() && FeathersCommonConfig.ENABLE_STRAIN.get() ? Math.max(0, view.maxStrain() - view.strain()) : 0;
        return cost <= view.availableStamina() + room ? SpendResult.OK : SpendResult.INSUFFICIENT;
    }

    /**
     * Pays a cost already checked with {@link FeathersData#canPay}, then fires Strain and exhaustion transitions.
     */
    static void payAndSettle(LivingEntity entity, FeathersData data, int cost, boolean strainEnabled) {
        boolean wasStrained = data.strain > 0;
        data.pay(cost);
        if (!wasStrained && data.strain > 0) NeoForge.EVENT_BUS.post(new StrainEvent.Started(entity));
        checkExhausted(entity, data, strainEnabled);
    }

    static void checkExhausted(LivingEntity entity, FeathersData data, boolean strainEnabled) {
        if (!data.exhausted && FeathersCommonConfig.ENABLE_EXHAUSTION.get() && data.isSpent(strainEnabled)) {
            data.exhausted = true;
            NeoForge.EVENT_BUS.post(new ExhaustionEvent.Exhausted(entity));
        }
    }

    /* Drains */

    @Override
    public SpendResult startDrain(LivingEntity entity, ResourceLocation source, double staminaPerTick, DrainOptions options) {
        if (!supports(entity) || isExempt(entity)) return SpendResult.EXEMPT;
        if (onClient(entity)) {
            ClientBridge bridge = clientBridge;
            return bridge == null || !bridge.isLocalPlayer(entity) ? SpendResult.EXEMPT
                    : simulateAgainst(bridge.localView(), (int) Math.ceil(staminaPerTick), options.allowStrain() ? SpendOptions.DEFAULT : SpendOptions.DEFAULT.withoutStrain());
        }

        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        long now = entity.level().getGameTime();

        FeathersData.Drain drain = data.drain(source);
        if (drain == null) {
            if (data.exhausted) return SpendResult.EXHAUSTED;
            int firstTick = effectiveCost(entity, data, source, Math.ceil(staminaPerTick));
            if (!data.canPay(firstTick, options.allowStrain(), FeathersCommonConfig.ENABLE_STRAIN.get())) return SpendResult.INSUFFICIENT;
            drain = new FeathersData.Drain(source);
            data.drains.add(drain);
            NeoForge.EVENT_BUS.post(new DrainEvent.Started(entity, source, staminaPerTick));
        }
        drain.perTick = Math.max(0, staminaPerTick);
        drain.allowStrain = options.allowStrain();
        drain.blocksRegen = options.blocksRegen();
        drain.timeoutTicks = options.timeoutTicks();
        drain.lastRefresh = now;
        return SpendResult.OK;
    }

    @Override
    public void stopDrain(LivingEntity entity, ResourceLocation source) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        for (int i = 0, n = data.drains.size(); i < n; i++) {
            if (data.drains.get(i).source.equals(source)) {
                data.drains.remove(i);
                NeoForge.EVENT_BUS.post(new DrainEvent.Stopped(entity, source, DrainEvent.Stopped.Reason.STOPPED));
                return;
            }
        }
    }

    @Override
    public boolean isDraining(LivingEntity entity, ResourceLocation source) {
        return supports(entity) && !onClient(entity) && data(entity).drain(source) != null;
    }

    /* Giving and setting */

    @Override
    public int gain(LivingEntity entity, ResourceLocation source, int stamina) {
        if (!supports(entity) || onClient(entity) || stamina <= 0) return 0;
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        GainEvent event = NeoForge.EVENT_BUS.post(new GainEvent(entity, source, stamina));
        if (event.isCanceled()) return 0;
        int gained = Math.min(event.getAmount(), Math.max(0, data.maxStamina - data.stamina));
        data.stamina += gained;
        FeathersTicker.checkRecovered(entity, data);
        return gained;
    }

    @Override
    public void setStamina(LivingEntity entity, int stamina) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        data.stamina = Math.clamp(stamina, 0, data.maxStamina);
        FeathersTicker.checkRecovered(entity, data);
    }

    @Override
    public void reset(LivingEntity entity) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        boolean wasStrained = data.strain > 0;
        data.stamina = data.maxStamina;
        data.strain = 0;
        data.regenDelay = 0;
        if (wasStrained) NeoForge.EVENT_BUS.post(new StrainEvent.Cleared(entity));
        if (data.exhausted) {
            data.exhausted = false;
            NeoForge.EVENT_BUS.post(new ExhaustionEvent.Recovered(entity));
        }
    }

    /* Bonuses, blocks, rest */

    private static long until(LivingEntity entity, int ticks) {
        return ticks < 0 ? FeathersData.FOREVER : entity.level().getGameTime() + ticks;
    }

    @Override
    public void addBonusStamina(LivingEntity entity, ResourceLocation source, int stamina, int ticks) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        if (stamina <= 0) data.removeBonus(source);
        else data.setBonus(source, stamina, until(entity, ticks));
        FeathersTicker.checkRecovered(entity, data);
    }

    @Override
    public void removeBonusStamina(LivingEntity entity, ResourceLocation source) {
        if (supports(entity) && !onClient(entity)) data(entity).removeBonus(source);
    }

    @Override
    public void blockRegen(LivingEntity entity, ResourceLocation source, int ticks) {
        if (supports(entity) && !onClient(entity)) FeathersData.setTimed(data(entity).regenBlocks, source, 0, until(entity, ticks));
    }

    @Override
    public void unblockRegen(LivingEntity entity, ResourceLocation source) {
        if (supports(entity) && !onClient(entity)) FeathersData.removeTimed(data(entity).regenBlocks, source);
    }

    @Override
    public void setRestBonus(LivingEntity entity, ResourceLocation source, double multiplier, int ticks) {
        if (supports(entity) && !onClient(entity)) FeathersData.setTimed(data(entity).restBonuses, source, multiplier, until(entity, ticks));
    }

    @Override
    public void removeRestBonus(LivingEntity entity, ResourceLocation source) {
        if (supports(entity) && !onClient(entity)) FeathersData.removeTimed(data(entity).restBonuses, source);
    }

    /* Climate and weight */

    @Override
    public Climate getClimate(LivingEntity entity) {
        return supports(entity) && !onClient(entity) ? data(entity).climate : Climate.NEUTRAL;
    }

    @Override
    public int getArmorWeight(LivingEntity entity) {
        return view(entity).weight();
    }

    @Override
    public double getPieceWeight(ItemStack stack) {
        return ArmorWeights.pieceWeight(stack);
    }

    @Override
    public void recalculateWeight(LivingEntity entity) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        data.weight = ArmorWeights.totalWeight(entity);
    }

    /* Extension points */

    @Override
    public void registerClimateProvider(ResourceLocation id, int priority, ClimateProvider provider) {
        Extensions.addClimate(id, priority, provider);
    }

    @Override
    public void registerRegenFactor(ResourceLocation id, RegenFactor factor) {
        Extensions.addRegenFactor(id, factor);
    }

    @Override
    public void registerWeightSource(ResourceLocation id, WeightSource source) {
        Extensions.addWeightSource(id, source);
    }

    @Override
    public void registerStaminaModifier(ResourceLocation id, int ordinal, StaminaModifier modifier) {
        Extensions.addModifier(id, ordinal, modifier);
    }

    @Override
    public void sync(LivingEntity entity) {
        if (supports(entity) && !onClient(entity)) data(entity).forceSync = true;
    }
}
