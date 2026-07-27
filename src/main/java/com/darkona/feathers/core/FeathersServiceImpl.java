package com.darkona.feathers.core;

import com.darkona.feathers.Feathers;
import com.darkona.feathers.api.*;
import com.darkona.feathers.api.event.DrainEvent;
import com.darkona.feathers.api.event.ExhaustionEvent;
import com.darkona.feathers.api.event.GainEvent;
import com.darkona.feathers.api.event.SpendEvent;
import com.darkona.feathers.api.event.StrainEvent;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersDataMaps;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.spi.FeathersService;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.network.FeathersNetwork;
import com.darkona.feathers.weight.ArmorWeights;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;


/**
 * The server-side implementation behind the API. Client-side calls go to {@link ClientBridge}.
 */
public final class FeathersServiceImpl implements FeathersService {

    public static final FeathersServiceImpl INSTANCE = new FeathersServiceImpl();

    /**
     * Supplies the local player's synchronized state for client-side API calls. The client installs this bridge.
     */
    public interface ClientBridge {
        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isLocalPlayer(LivingEntity entity);

        /**
         * The client's feathers for the local player or its mount, or null for any other entity.
         */
        @Nullable
        FeathersView clientView(LivingEntity entity);

        FeathersView localView();

        /**
         * Checks {@code cost}, already through the usage multiplier and the modifiers, on the local view like
         * {@link #simulateAgainst} and pays it the way the server would, so the HUD reacts before the server's sync.
         * EXEMPT until the first sync.
         */
        SpendResult payPredicted(int cost, SpendOptions options);
    }

    private static volatile ClientBridge clientBridge;

    private FeathersServiceImpl() {}

    public static void setClientBridge(ClientBridge bridge) {
        clientBridge = bridge;
    }

    /* Access */

    /**
     * Supports all players and configured mounts, including horses, donkeys, mules, and camels.
     */
    @Override
    public boolean supports(LivingEntity entity) {
        return entity instanceof Player || isMount(entity);
    }

    /**
     * Horses and anything extending them, plus the entity types in the {@code greenfeathers:mounts} tag or the
     * {@code greenfeathers:mount_stats} data map: modpacks can give feathers to any creature.
     */
    public static boolean isMount(LivingEntity entity) {
        if (entity instanceof Player || !FeathersServerConfig.ENABLE_MOUNTS.get()) return false;
        // Asked for every living entity every tick: the verdict is per type, worked out once and kept until tags,
        // data maps or the config change (see invalidateMountTypes).
        int id = BuiltInRegistries.ENTITY_TYPE.getId(entity.getType());
        byte[] verdicts = mountTypes;
        if (verdicts.length == 0) {
            invalidateMountTypes();
            verdicts = mountTypes;
        }
        if (id < 0 || id >= verdicts.length) return computeMount(entity);
        byte verdict = verdicts[id];
        if (verdict == UNKNOWN_TYPE) {
            verdict = computeMount(entity) ? MOUNT_TYPE : NOT_MOUNT_TYPE;
            verdicts[id] = verdict;
        }
        return verdict == MOUNT_TYPE;
    }

    private static final byte UNKNOWN_TYPE = 0;
    private static final byte MOUNT_TYPE = 1;
    private static final byte NOT_MOUNT_TYPE = 2;
    private static volatile byte[] mountTypes = new byte[0];

    private static boolean computeMount(LivingEntity entity) {
        if (entity.getType().is(FeathersIds.NO_FEATHERS)) return false;
        boolean mount = entity instanceof AbstractHorse || entity.getType().is(FeathersIds.MOUNTS)
                || BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType())
                .getData(FeathersDataMaps.MOUNT_STATS) != null;
        return mount && entity.getAttribute(FeathersAttributes.MAX_FEATHERS) != null;
    }

    /** Tags, data maps or the config changed: every entity type's mount verdict is worked out again. */
    public static void invalidateMountTypes() {
        mountTypes = new byte[BuiltInRegistries.ENTITY_TYPE.size()];
    }

    /**
     * The creature's mount tuning from the data map, or {@link MountStats#DEFAULT} (the config) without an entry.
     */
    public static MountStats mountStats(LivingEntity entity) {
        MountStats stats = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity.getType())
                .getData(FeathersDataMaps.MOUNT_STATS);
        return stats != null ? stats : MountStats.DEFAULT;
    }

    /**
     * Checks whether a player bypasses the stamina system in Creative or Spectator mode.
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
            FeathersView known = bridge != null ? bridge.clientView(entity) : null;
            return known != null ? known : FeathersView.NONE;
        }
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        return data;
    }

    /**
     * Maximum stamina and weight come from attributes and equipment. The first access after joining or loading reads
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
        data.weight = ArmorWeights.totalWeight(entity, data.weightSplit);
        data.forceSync = true;
    }

    /**
     * Reads the max feathers and max strain attributes.
     *
     * @return whether either changed
     */
    @SuppressWarnings("UnusedReturnValue")
    static boolean refreshMaximums(LivingEntity entity, FeathersData data) {
        AttributeInstance maxFeathers = data.maxFeathersAttribute(entity);
        AttributeInstance maxStrain = data.maxStrainAttribute(entity);
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
        return effectiveCost(entity, data, data.usageAttribute(entity), source, baseCost);
    }

    /**
     * A base cost after the usage multiplier and the stamina modifiers, against any view of the entity's feathers.
     * The client prices its predictions with it: the usage multiplier is a synced attribute, and modifiers are
     * registered on both sides.
     */
    public static int effectiveCost(LivingEntity entity, FeathersView feathers, @Nullable AttributeInstance usage,
                                    ResourceLocation source, double baseCost) {
        // Clamped before narrowing: an "everything" cost times a multiplier would wrap negative and cost nothing.
        int cost = (int) Math.min(Integer.MAX_VALUE, Math.round(baseCost * (usage != null ? usage.getValue() : 1.0)));
        for (Extensions.ModifierEntry modifier : Extensions.modifiers()) {
            cost = modifier.modifier().modifyCost(entity, feathers, source, cost);
        }
        return Math.max(0, cost);
    }

    /** A cost priced on the client against its view of the local player's feathers. */
    private static int clientCost(LivingEntity entity, FeathersView feathers, ResourceLocation source, double baseCost) {
        return effectiveCost(entity, feathers, entity.getAttribute(FeathersAttributes.USAGE_MULTIPLIER), source, baseCost);
    }

    @Override
    public SpendResult spend(LivingEntity entity, ResourceLocation source, int stamina, SpendOptions options) {
        if (!supports(entity) || isExempt(entity)) return SpendResult.EXEMPT;

        if (onClient(entity)) {
            ClientBridge bridge = clientBridge;
            if (bridge == null || !bridge.isLocalPlayer(entity)) return SpendResult.EXEMPT;
            FeathersView local = bridge.localView();
            int cost = stamina > 0 ? clientCost(entity, local, source, stamina) : 0;
            if (options.simulate()) return simulateAgainst(local, cost, options.allowStrain(), options.ignoreExhaustion());
            return bridge.payPredicted(cost, options);
        }

        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        boolean strainEnabled = FeathersServerConfig.ENABLE_STRAIN.get();

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
        data.logSpend(source, cost, entity.level().getGameTime());

        applyRegenDelay(data, options);

        NeoForge.EVENT_BUS.post(new SpendEvent.Post(entity, source, cost, SpendResult.OK));
        if (FeathersServerConfig.DEBUG_MODE.get()) {
            Feathers.LOGGER.info("{} spent {} stamina on {}", entity.getName().getString(), cost, source);
            FeathersNetwork.sendSpendDebug(entity, source, cost);
        }
        return SpendResult.OK;
    }

    private static void applyRegenDelay(FeathersData data, SpendOptions options) {
        data.regenDelay = regenDelayAfter(data.regenDelay, options);
    }

    /**
     * A regeneration delay after a spend with {@code options}: the spend's delay (or the config's) added to
     * {@code current}, up to max_cooldown. Added in long: a huge delay from the API would wrap negative.
     */
    public static int regenDelayAfter(int current, SpendOptions options) {
        int delay = options.regenDelayTicks() < 0 ? FeathersServerConfig.DEFAULT_USAGE_COOLDOWN.get() : options.regenDelayTicks();
        return (int) Math.min((long) current + delay, FeathersServerConfig.MAX_COOLDOWN.get() * 20L);
    }

    /**
     * Checks {@code cost}, already priced, against a view: exhaustion, then stamina and bonus plus the strain room the
     * spend may use. For views that are not the server's, such as the client's copy.
     */
    public static SpendResult simulateAgainst(FeathersView view, int cost, boolean allowStrain, boolean ignoreExhaustion) {
        if (view.exhausted() && !ignoreExhaustion) return SpendResult.EXHAUSTED;
        int room = allowStrain && FeathersServerConfig.ENABLE_STRAIN.get() ? Math.max(0, view.maxStrain() - view.strain()) : 0;
        return cost <= view.availableStamina() + room ? SpendResult.OK : SpendResult.INSUFFICIENT;
    }

    /**
     * Pays a cost already checked with {@link FeathersData#canPay}, then fires strain and exhaustion transitions.
     */
    static void payAndSettle(LivingEntity entity, FeathersData data, int cost, boolean strainEnabled) {
        boolean wasStrained = data.strain > 0;
        data.pay(cost);
        if (!wasStrained && data.strain > 0) NeoForge.EVENT_BUS.post(new StrainEvent.Started(entity));
        checkExhausted(entity, data, strainEnabled);
    }

    static void checkExhausted(LivingEntity entity, FeathersData data, boolean strainEnabled) {
        if (!data.exhausted && data.maxStamina > 0 && FeathersServerConfig.ENABLE_EXHAUSTION.get() && data.isSpent(strainEnabled)) {
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
            if (bridge == null || !bridge.isLocalPlayer(entity)) return SpendResult.EXEMPT;
            FeathersView local = bridge.localView();
            return simulateAgainst(local, clientCost(entity, local, source, Math.ceil(staminaPerTick)), options.allowStrain(), false);
        }

        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        long now = entity.level().getGameTime();

        FeathersData.Drain drain = data.drain(source);
        if (drain == null) {
            if (data.exhausted) return SpendResult.EXHAUSTED;
            int firstTick = effectiveCost(entity, data, source, Math.ceil(staminaPerTick));
            if (!data.canPay(firstTick, options.allowStrain(), FeathersServerConfig.ENABLE_STRAIN.get())) return SpendResult.INSUFFICIENT;
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
        int i = FeathersData.indexOf(data.drains, source);
        if (i < 0) return;
        data.drains.remove(i);
        NeoForge.EVENT_BUS.post(new DrainEvent.Stopped(entity, source, DrainEvent.Stopped.Reason.STOPPED));
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
        // Strain is stamina owed: paid back first, like regeneration does. Otherwise a gift while strained would leave
        // stamina and strain side by side, which nothing else produces and the HUD can't draw.
        int recovered = Math.min(data.strain, event.getAmount());
        data.strain -= recovered;
        int gained = Math.clamp(data.maxStamina - data.stamina, 0, event.getAmount() - recovered);
        data.stamina += gained;
        if (recovered > 0 && data.strain == 0) NeoForge.EVENT_BUS.post(new StrainEvent.Cleared(entity));
        FeathersTicker.checkRecovered(entity, data);
        return recovered + gained;
    }

    @Override
    public void setStamina(LivingEntity entity, int stamina) {
        if (!supports(entity) || onClient(entity)) return;
        FeathersData data = data(entity);
        ensureInitialized(entity, data);
        data.stamina = Math.clamp(stamina, 0, data.maxStamina);
        // Strain is owed from an empty bar: a bar set above zero owes nothing.
        if (data.stamina > 0 && data.strain > 0) {
            data.strain = 0;
            NeoForge.EVENT_BUS.post(new StrainEvent.Cleared(entity));
        }
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
        data.weight = ArmorWeights.totalWeight(entity, data.weightSplit);
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
