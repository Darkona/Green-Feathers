package com.darkona.feathers.core;

import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.event.DrainEvent;
import com.darkona.feathers.api.event.ExhaustionEvent;
import com.darkona.feathers.api.event.StrainEvent;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.api.RestState;
import com.darkona.feathers.climate.ClimateEffects;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.ModEffects;
import com.darkona.feathers.network.FeathersNetwork;
import com.darkona.feathers.weight.ArmorWeights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Runs feathers for every entity that has them: drains, regeneration, Strain, exhaustion, rest, climate, weight,
 * and syncing the owning client. Server side only; the client shows what the server syncs.
 */
@EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class FeathersTicker {

    private static final int ATTRIBUTE_INTERVAL = 10;
    private static final int REGEN_FACTOR_INTERVAL = 20;
    private static final double STILL_EPSILON_SQR = 1.0E-4;
    private static final int HUNGRY_FOOD_LEVEL = 6;
    private static final ResourceLocation REGEN_FACTORS = id("regen_factors");

    private FeathersTicker() {}

    /* Mod bus, registered from the mod constructor */

    public static void addAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, FeathersAttributes.MAX_FEATHERS);
        event.add(EntityType.PLAYER, FeathersAttributes.MAX_STRAIN);
        event.add(EntityType.PLAYER, FeathersAttributes.FEATHERS_PER_SECOND);
        event.add(EntityType.PLAYER, FeathersAttributes.USAGE_MULTIPLIER);
        event.add(EntityType.PLAYER, FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER);
    }

    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != FeathersCommonConfig.SPEC || event instanceof ModConfigEvent.Unloading) return;
        ArmorWeights.invalidate();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.execute(() -> server.getPlayerList().getPlayers().forEach(FeathersTicker::refreshFromConfig));
    }

    /* Lifecycle */

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)
                || !FeathersServiceImpl.INSTANCE.supports(entity)) return;
        refreshFromConfig(entity);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        FeathersServiceImpl.INSTANCE.reset(event.getEntity());
    }

    @SubscribeEvent
    public static void onClone(PlayerEvent.Clone event) {
        // NeoForge copied the attachment unless this was a death; either way the client must hear about it.
        FeathersServiceImpl.data(event.getEntity()).forceSync = true;
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        FeathersServiceImpl.data(event.getEntity()).forceSync = true;
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        FeathersServiceImpl.data(event.getEntity()).forceSync = true;
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        if (FeathersCommonConfig.SLEEPING_ALWAYS_RESTORES_FEATHERS.get() && !event.getEntity().level().isClientSide()) {
            FeathersServiceImpl.INSTANCE.reset(event.getEntity());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot().isArmor() && !event.getEntity().level().isClientSide()) {
            FeathersServiceImpl.INSTANCE.recalculateWeight(event.getEntity());
        }
    }

    /**
     * Tags and data maps, both armor weight sources, change on datapack reload.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        ArmorWeights.invalidate();
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.getPlayerList().getPlayers().forEach(FeathersServiceImpl.INSTANCE::recalculateWeight);
    }

    /**
     * Attribute bases come from the common config.
     */
    private static void refreshFromConfig(LivingEntity entity) {
        setBase(entity.getAttribute(FeathersAttributes.MAX_FEATHERS), FeathersCommonConfig.MAX_FEATHERS.get());
        setBase(entity.getAttribute(FeathersAttributes.MAX_STRAIN), FeathersCommonConfig.MAX_STRAIN.get());
        setBase(entity.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND), FeathersCommonConfig.REGEN_FEATHERS_PER_SECOND.get());
        FeathersData data = FeathersServiceImpl.data(entity);
        FeathersServiceImpl.ensureInitialized(entity, data);
        FeathersServiceImpl.refreshMaximums(entity, data);
        data.weight = ArmorWeights.totalWeight(entity);
        data.forceSync = true;
    }

    private static void setBase(AttributeInstance attribute, double value) {
        if (attribute != null && attribute.getBaseValue() != value) attribute.setBaseValue(value);
    }

    /* Tick */

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive()) return;
        tick(player);
    }

    /**
     * One tick of an entity's feathers. Exempt entities (creative players) only keep their client in sync.
     */
    public static void tick(LivingEntity entity) {
        FeathersData data = FeathersServiceImpl.data(entity);
        FeathersServiceImpl.ensureInitialized(entity, data);

        if (!FeathersServiceImpl.isExempt(entity)) {
            long now = entity.level().getGameTime();
            boolean strainEnabled = FeathersCommonConfig.ENABLE_STRAIN.get();
            int staminaBefore = data.stamina;
            boolean strainedBefore = data.strain > 0;

            if (entity.tickCount % ATTRIBUTE_INTERVAL == 0) tickAttributes(entity, data);
            if (entity.tickCount % REGEN_FACTOR_INTERVAL == 0) applyRegenFactors(entity, data);
            if (entity.tickCount % ClimateEffects.INTERVAL == 0) {
                data.climate = ClimateEffects.evaluate(entity);
                ClimateEffects.apply(entity, data.climate);
            }

            data.expire(now);
            updateRest(entity, data);
            tickDrains(entity, data, now, strainEnabled);
            regenerate(entity, data);

            data.lastDelta = data.stamina - staminaBefore;
            boolean strained = data.strain > 0;
            if (strainedBefore != strained) {
                NeoForge.EVENT_BUS.post(strained ? new StrainEvent.Started(entity) : new StrainEvent.Cleared(entity));
            }
            if (!strainEnabled && data.strain > 0) data.strain = 0;

            FeathersServiceImpl.checkExhausted(entity, data, strainEnabled);
            checkRecovered(entity, data);
            updateIndicators(entity, data, strained);
        }

        syncIfChanged(entity, data);
    }

    private static void tickAttributes(LivingEntity entity, FeathersData data) {
        FeathersServiceImpl.refreshMaximums(entity, data);

        // The multiplier changes through other mods' items (e.g. a Curios ring) with no event of its own.
        AttributeInstance multiplier = entity.getAttribute(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER);
        double value = multiplier != null ? multiplier.getValue() : 1.0;
        if (value != data.lastWeightMultiplier) {
            data.lastWeightMultiplier = value;
            data.weight = ArmorWeights.totalWeight(entity);
        }

        if (entity.hasEffect(FeathersMobEffects.ENDURANCE) && data.bonus(ModEffects.ENDURANCE_BONUS) == null) {
            entity.removeEffect(FeathersMobEffects.ENDURANCE);
        }
    }

    /**
     * Sums every regeneration factor into one transient modifier, touched only when the sum moves.
     */
    private static void applyRegenFactors(LivingEntity entity, FeathersData data) {
        Extensions.RegenEntry[] factors = Extensions.regenFactors();
        AttributeInstance regen = entity.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND);
        if (regen == null) return;

        double sum = 0;
        for (Extensions.RegenEntry factor : factors) {
            sum += factor.factor().feathersPerSecond(entity, data);
        }
        AttributeModifier current = regen.getModifier(REGEN_FACTORS);
        if (sum == 0) {
            if (current != null) regen.removeModifier(REGEN_FACTORS);
        } else if (current == null || current.amount() != sum) {
            regen.addOrUpdateTransientModifier(new AttributeModifier(REGEN_FACTORS, sum, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void updateRest(LivingEntity entity, FeathersData data) {
        double x = entity.getX(), y = entity.getY(), z = entity.getZ();
        double dx = x - data.lastX, dy = y - data.lastY, dz = z - data.lastZ;
        data.lastX = x;
        data.lastY = y;
        data.lastZ = z;

        if (!FeathersCommonConfig.ENABLE_REST.get()) {
            data.restState = RestState.NONE;
            data.restMultiplier = 1.0;
            return;
        }

        boolean moved = dx * dx + dy * dy + dz * dz > STILL_EPSILON_SQR || entity.isSwimming() || entity.isFallFlying();
        data.stillTicks = moved ? 0 : data.stillTicks + 1;

        double multiplier;
        if (entity.isPassenger()) {
            data.restState = RestState.SITTING;
            multiplier = FeathersCommonConfig.REST_SITTING_MULTIPLIER.get();
        } else if (data.stillTicks >= FeathersCommonConfig.REST_STILL_TICKS.get()) {
            boolean crouching = entity.isCrouching();
            data.restState = crouching ? RestState.CROUCHING : RestState.STILL;
            multiplier = crouching ? FeathersCommonConfig.REST_CROUCHING_MULTIPLIER.get() : FeathersCommonConfig.REST_STILL_MULTIPLIER.get();
        } else {
            data.restState = RestState.NONE;
            multiplier = 1.0;
        }

        for (int i = 0, n = data.restBonuses.size(); i < n; i++) {
            multiplier = Math.max(multiplier, data.restBonuses.get(i).value);
        }
        data.restMultiplier = multiplier;
    }

    private static void tickDrains(LivingEntity entity, FeathersData data, long now, boolean strainEnabled) {
        for (int i = data.drains.size() - 1; i >= 0; i--) {
            FeathersData.Drain drain = data.drains.get(i);

            DrainEvent.Stopped.Reason stop = null;
            if (drain.timeoutTicks > 0 && now - drain.lastRefresh > drain.timeoutTicks) {
                stop = DrainEvent.Stopped.Reason.TIMED_OUT;
            } else if (data.exhausted) {
                stop = DrainEvent.Stopped.Reason.EXHAUSTED;
            } else {
                drain.carry += drain.perTick;
                int base = (int) drain.carry;
                if (base > 0) {
                    drain.carry -= base;
                    int cost = FeathersServiceImpl.effectiveCost(entity, data, drain.source, base);
                    if (data.canPay(cost, drain.allowStrain, strainEnabled)) {
                        FeathersServiceImpl.payAndSettle(entity, data, cost, strainEnabled);
                    } else {
                        stop = DrainEvent.Stopped.Reason.INSUFFICIENT;
                        FeathersServiceImpl.checkExhausted(entity, data, strainEnabled);
                    }
                }
            }

            if (stop != null) {
                data.drains.remove(i);
                NeoForge.EVENT_BUS.post(new DrainEvent.Stopped(entity, drain.source, stop));
            }
        }
    }

    /**
     * Regeneration pays Strain back first (faster while resting), then refills stamina. Fractions carry over, so
     * any rate works however slow.
     */
    private static void regenerate(LivingEntity entity, FeathersData data) {
        boolean blocked = data.isRegenBlocked();
        if (data.regenDelay > 0) data.regenDelay--;

        AttributeInstance attribute = entity.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND);
        double perTick = attribute != null ? Stamina.perTick(attribute.getValue()) : 0.0;

        if (perTick > 0) {
            boolean hungry = entity instanceof Player player && FeathersCommonConfig.REGEN_USES_HUNGER.get()
                    && player.getFoodData().getFoodLevel() <= HUNGRY_FOOD_LEVEL;
            if (blocked || hungry) {
                data.regenCarry = 0;
                return;
            }
            if (FeathersCommonConfig.REST_BOOSTS_REGEN.get()) perTick *= data.restMultiplier;
        }

        data.regenCarry += perTick;
        int regen = (int) data.regenCarry;
        data.regenCarry -= regen;

        for (Extensions.ModifierEntry modifier : Extensions.modifiers()) {
            regen = modifier.modifier().modifyRegen(entity, data, regen);
        }

        if (regen < 0) {
            data.stamina = Math.max(0, data.stamina + regen);
            return;
        }
        if (regen == 0) return;

        int recovered = 0;
        if (data.strain > 0) {
            // Resting pays Strain back faster; don't apply it twice when it already boosted regeneration.
            double rest = FeathersCommonConfig.REST_BOOSTS_REGEN.get() ? 1.0 : data.restMultiplier;
            int recovery = (int) Math.round(regen * rest);
            recovered = Math.min(data.strain, recovery);
            data.strain -= recovered;
            regen = (int) ((recovery - recovered) / rest);
        }

        int gained = Math.min(regen, Math.max(0, data.maxStamina - data.stamina));
        data.stamina += gained;
        data.totalRegenerated += gained + recovered;

        if (entity instanceof Player player && FeathersCommonConfig.REGEN_USES_HUNGER.get() && gained + recovered > 0) {
            player.causeFoodExhaustion((float) ((gained + recovered) / (double) Stamina.PER_FEATHER * FeathersCommonConfig.HUNGER_PER_FEATHER.get()));
        }
    }

    /**
     * Ends exhaustion once there is no Strain left and the share of the bar set in the config is back, or at once
     * when exhaustion is turned off.
     */
    static void checkRecovered(LivingEntity entity, FeathersData data) {
        if (!data.exhausted) return;
        boolean recovered = !FeathersCommonConfig.ENABLE_EXHAUSTION.get()
                || data.strain == 0 && data.availableStamina() >= data.maxStamina * FeathersCommonConfig.EXHAUSTION_RECOVERY.get();
        if (recovered) {
            data.exhausted = false;
            NeoForge.EVENT_BUS.post(new ExhaustionEvent.Recovered(entity));
        }
    }

    /**
     * The Strained effect shows while Strain is being paid back; touched only on a change, since adding an effect
     * sends it to the client.
     */
    private static void updateIndicators(LivingEntity entity, FeathersData data, boolean strained) {
        if (strained == entity.hasEffect(FeathersMobEffects.STRAINED)) return;
        if (strained) {
            entity.addEffect(new MobEffectInstance(FeathersMobEffects.STRAINED, MobEffectInstance.INFINITE_DURATION, 0, false, false, true));
        } else {
            entity.removeEffect(FeathersMobEffects.STRAINED);
        }
    }

    /**
     * Sends the client a snapshot when something it shows changed: whole feathers, Strain, bonus, weight, maximums,
     * exhaustion, the regeneration pause, rest.
     */
    private static void syncIfChanged(LivingEntity entity, FeathersData data) {
        if (!(entity instanceof ServerPlayer player)) return;

        int shownStamina = FeathersCommonConfig.DEBUG_MODE.get() ? data.stamina : Stamina.toFeathers(data.stamina);
        int shownStrain = Stamina.toFeathersCeil(data.strain);
        int shownBonus = Stamina.toFeathersCeil(data.bonusStamina());
        boolean delayed = data.regenDelay > 0;

        if (!data.forceSync && shownStamina == data.syncedStamina && data.maxStamina == data.syncedMax
                && shownStrain == data.syncedStrain && data.maxStrain == data.syncedMaxStrain && shownBonus == data.syncedBonus
                && data.weight == data.syncedWeight && data.exhausted == data.syncedExhausted && delayed == data.syncedDelayed
                && data.restState == data.syncedRest) return;

        data.forceSync = false;
        data.syncedStamina = shownStamina;
        data.syncedMax = data.maxStamina;
        data.syncedStrain = shownStrain;
        data.syncedMaxStrain = data.maxStrain;
        data.syncedBonus = shownBonus;
        data.syncedWeight = data.weight;
        data.syncedExhausted = data.exhausted;
        data.syncedDelayed = delayed;
        data.syncedRest = data.restState;
        FeathersNetwork.sendSync(player, data);
    }
}
