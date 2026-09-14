package com.darkona.feathersoffatigue.core;

import com.darkona.feathersoffatigue.api.RestState;
import com.darkona.feathersoffatigue.api.Stamina;
import com.darkona.feathersoffatigue.api.event.DrainEvent;
import com.darkona.feathersoffatigue.api.event.ExhaustionEvent;
import com.darkona.feathersoffatigue.api.event.RegenEvent;
import com.darkona.feathersoffatigue.api.event.StrainEvent;
import com.darkona.feathersoffatigue.api.registry.FeathersAttributes;
import com.darkona.feathersoffatigue.api.registry.FeathersIds;
import com.darkona.feathersoffatigue.api.registry.FeathersMobEffects;
import com.darkona.feathersoffatigue.climate.ClimateEffects;
import com.darkona.feathersoffatigue.config.FeathersServerConfig;
import com.darkona.feathersoffatigue.effect.FeathersMobEffect;
import com.darkona.feathersoffatigue.effect.ModEffects;
import com.darkona.feathersoffatigue.mount.MountExertion;
import com.darkona.feathersoffatigue.mount.MountTraits;
import com.darkona.feathersoffatigue.network.FeathersNetwork;
import com.darkona.feathersoffatigue.weight.ArmorWeights;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import com.darkona.feathersoffatigue.data.DataMaps;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

import static com.darkona.feathersoffatigue.api.registry.FeathersIds.id;

/**
 * Runs feathers for every entity that has them: drains, regeneration, strain, exhaustion, rest, climate, weight,
 * and client synchronization. This class runs only on the server. The client displays the synchronized state.
 */
@Mod.EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class FeathersTicker {

    private static final int ATTRIBUTE_INTERVAL = 10;
    private static final int REGEN_FACTOR_INTERVAL = 20;
    private static final double STILL_EPSILON_SQR = 1.0E-4;
    private static final ResourceLocation REGEN_FACTORS = id("regen_factors");
    private static final UUID REGEN_FACTORS_ID = modifierId(REGEN_FACTORS);

    /** Attribute modifier ids, stable across saves: from the modifier's resource location. */
    public static UUID modifierId(ResourceLocation id) {
        return UUID.nameUUIDFromBytes(id.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private FeathersTicker() {}

    /* Mod bus, registered from the mod constructor */

    /**
     * Every living entity type gets the feathers attributes, so any mod's creature can become a mount (by extending
     * the horse or through the mounts tag). Instances are only created for entities that read them.
     */
    public static void addAttributes(EntityAttributeModificationEvent event) {
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (event.has(type, FeathersAttributes.MAX_FEATHERS.get())) continue;
            event.add(type, FeathersAttributes.MAX_FEATHERS.get());
            event.add(type, FeathersAttributes.MAX_STRAIN.get());
            event.add(type, FeathersAttributes.FEATHERS_PER_SECOND.get());
            event.add(type, FeathersAttributes.USAGE_MULTIPLIER.get());
            event.add(type, FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get());
        }
    }

    public static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != FeathersServerConfig.SPEC) return;
        ArmorWeights.invalidate();
        FeathersServiceImpl.invalidateMountTypes();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.execute(() -> server.getPlayerList().getPlayers().forEach(FeathersTicker::refreshFromConfig));
    }

    /* Lifecycle */

    @SubscribeEvent
    public static void onJoin(EntityJoinWorldEvent event) {
        if (event.getWorld().isClientSide() || !(event.getEntity() instanceof LivingEntity entity)
                || !FeathersServiceImpl.INSTANCE.supports(entity)) return;
        refreshFromConfig(entity);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        FeathersServiceImpl.INSTANCE.reset(event.getPlayer());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onClone(PlayerEvent.Clone event) {
        // FeathersAttachments copied the capability unless this was a death. In both cases, synchronize the resulting state.
        FeathersServiceImpl.data(event.getPlayer()).forceSync = true;
    }

    @SubscribeEvent
    public static void onLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        FeathersServiceImpl.data(event.getPlayer()).forceSync = true;
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        FeathersServiceImpl.data(event.getPlayer()).forceSync = true;
    }

    @SubscribeEvent
    public static void onWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getPlayer();
        if (player.level.isClientSide() || !FeathersServerConfig.SLEEPING_ALWAYS_RESTORES_FEATHERS.get()) return;
        // Only a night slept through. The level wakes everyone with no sleeper list update once it skipped the night.
        // Every other wake-up updates the list: "Leave Bed", the day breaking over a player still in bed, and
        // stopSleeping(), which wakes at once (hurt, death, a broken bed, or a sleep mod after skipping the night on
        // its own). Those count only by day: "Leave Bed" after five seconds with the other players up must not
        // restore everything at night. A disconnect wakes at once with no update, and never counts.
        if (!player.isSleepingLongEnough()) return;
        if (event.updateWorld() ? !player.level.isDay() : event.wakeImmediately()) return;
        FeathersServiceImpl.INSTANCE.reset(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot().getType() == EquipmentSlot.Type.ARMOR && !event.getEntityLiving().level.isClientSide()
                && FeathersServiceImpl.INSTANCE.supports(event.getEntityLiving())) {
            FeathersServiceImpl.INSTANCE.recalculateWeight(event.getEntityLiving());
        }
    }

    /**
     * Tags and data maps, both armor weight sources, change on datapack reload.
     */
    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        DataMaps.onTagsChanged();
        ArmorWeights.invalidate();
        FeathersServiceImpl.invalidateMountTypes();
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) server.getPlayerList().getPlayers().forEach(FeathersServiceImpl.INSTANCE::recalculateWeight);
    }

    /**
     * Attribute bases come from the server config.
     */
    static void refreshFromConfig(LivingEntity entity) {
        boolean player = entity instanceof Player;
        // A mount's maximum is a hidden trait rolled once. A player's maximum comes from the configuration.
        FeathersData data = FeathersServiceImpl.data(entity);
        if (player) configBase(entity, data, FeathersAttributes.MAX_FEATHERS, BASE_MAX, FeathersServerConfig.MAX_FEATHERS.get());
        else MountTraits.ensureRolled(entity);
        configBase(entity, data, FeathersAttributes.MAX_STRAIN, BASE_STRAIN, FeathersServerConfig.MAX_STRAIN.get());
        configBase(entity, data, FeathersAttributes.FEATHERS_PER_SECOND, BASE_REGEN, player ? FeathersServerConfig.REGEN_FEATHERS_PER_SECOND.get()
                : FeathersServiceImpl.mountTuning(entity).regenPerSecond());
        FeathersServiceImpl.ensureInitialized(entity, data);
        FeathersServiceImpl.refreshMaximums(entity, data);
        data.weight = ArmorWeights.totalWeight(entity, data.weightSplit);
        data.forceSync = true;
    }

    /* The config value each base was last set to, in the saved counters. */
    private static final String BASE_MAX = "config_base.max_feathers";
    private static final String BASE_STRAIN = "config_base.max_strain";
    private static final String BASE_REGEN = "config_base.feathers_per_second";

    /**
     * Sets an attribute base from the config, unless something else (a command, the API) changed it since the config
     * last set it: that value is kept across rejoins, dimension changes and config reloads.
     */
    private static void configBase(LivingEntity entity, FeathersData data, RegistryObject<Attribute> attribute, String key, double value) {
        AttributeInstance instance = entity.getAttribute(attribute.get());
        if (instance == null) return;
        if (data.counters.containsKey(key) && instance.getBaseValue() != data.getCounter(key)) return;
        if (instance.getBaseValue() != value) instance.setBaseValue(value);
        data.setCounter(key, value);
    }

    /* Tick */

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || event.side.isClient() || !player.isAlive()) return;
        tick(player);
    }

    /**
     * One tick of an entity's feathers. Exempt entities (creative players) only keep their client in sync.
     */
    public static void tick(LivingEntity entity) {
        FeathersData data = FeathersServiceImpl.data(entity);
        // A creature that became a mount after it joined (mounts turned on, a datapack reload) missed onJoin: roll its
        // trait and set its bases now.
        if (!data.initialized && !(entity instanceof Player)) refreshFromConfig(entity);
        FeathersServiceImpl.ensureInitialized(entity, data);

        if (!FeathersServiceImpl.isExempt(entity)) {
            long now = entity.level.getGameTime();
            boolean strainEnabled = FeathersServerConfig.ENABLE_STRAIN.get();
            int staminaBefore = data.stamina;
            boolean strainedBefore = data.strain > 0;

            if (entity.tickCount % ATTRIBUTE_INTERVAL == 0) tickAttributes(entity, data);
            if (entity.tickCount % REGEN_FACTOR_INTERVAL == 0) applyRegenFactors(entity, data);
            // Climate currently affects only players. Mounts use stamina without climate effects.
            if (entity instanceof Player && entity.tickCount % ClimateEffects.INTERVAL == 0) {
                data.climate = ClimateEffects.evaluate(entity);
                ClimateEffects.apply(entity, data.climate);
            }

            data.expire(now);
            updateRest(entity, data);
            tickDrains(entity, data, now, strainEnabled);
            regenerate(entity, data);

            data.lastDelta = data.stamina - staminaBefore;
            // Strain turned off in the config while strained: dropped here, before the check below, so its clearing
            // posts the event and the strained effect goes with it.
            if (!strainEnabled && data.strain > 0) data.strain = 0;
            boolean strained = data.strain > 0;
            // A spend or drain starts strain and posts its event. This path only recovers existing strain.
            if (strainedBefore && !strained) MinecraftForge.EVENT_BUS.post(new StrainEvent.Cleared(entity));

            FeathersServiceImpl.checkExhausted(entity, data, strainEnabled);
            checkRecovered(entity, data);
            updateIndicators(entity, data, strained);
        }

        syncIfChanged(entity, data);
    }

    private static void tickAttributes(LivingEntity entity, FeathersData data) {
        FeathersServiceImpl.refreshMaximums(entity, data);

        // The multiplier changes through other mods' items (e.g. a Curios ring) with no event of its own.
        AttributeInstance multiplier = data.weightMultiplierAttribute(entity);
        double value = multiplier != null ? multiplier.getValue() : 1.0;
        if (value != data.lastWeightMultiplier) {
            data.lastWeightMultiplier = value;
            data.weight = ArmorWeights.totalWeight(entity, data.weightSplit);
        }

        if (entity.hasEffect(FeathersMobEffects.ENDURANCE.get()) && data.bonus(ModEffects.ENDURANCE_BONUS) == null) {
            entity.removeEffect(FeathersMobEffects.ENDURANCE.get());
        }
    }

    /**
     * Sums every regeneration factor into one transient modifier, touched only when the sum moves.
     */
    private static void applyRegenFactors(LivingEntity entity, FeathersData data) {
        Extensions.RegenEntry[] factors = Extensions.regenFactors();
        AttributeInstance regen = data.regenAttribute(entity);
        if (regen == null) return;

        double sum = 0;
        for (Extensions.RegenEntry factor : factors) {
            sum += factor.factor().feathersPerSecond(entity, data);
        }
        AttributeModifier current = regen.getModifier(REGEN_FACTORS_ID);
        if (sum == 0) {
            if (current != null) regen.removeModifier(REGEN_FACTORS_ID);
        } else if (current == null || current.getAmount() != sum) {
            if (current != null) regen.removeModifier(REGEN_FACTORS_ID);
            regen.addTransientModifier(new AttributeModifier(REGEN_FACTORS_ID, REGEN_FACTORS.toString(), sum, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void updateRest(LivingEntity entity, FeathersData data) {
        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        double dx = x - data.lastX;
        double dy = y - data.lastY;
        double dz = z - data.lastZ;
        data.lastX = x;
        data.lastY = y;
        data.lastZ = z;

        if (!FeathersServerConfig.ENABLE_REST.get()) {
            data.restState = RestState.NONE;
            data.restMultiplier = 1.0;
            return;
        }

        boolean moved = dx * dx + dy * dy + dz * dz > STILL_EPSILON_SQR || entity.isSwimming() || entity.isFallFlying();
        data.stillTicks = moved ? 0 : data.stillTicks + 1;

        double multiplier;
        if (entity.isPassenger()) {
            data.restState = RestState.SITTING;
            multiplier = FeathersServerConfig.REST_SITTING_MULTIPLIER.get();
        } else if (data.stillTicks >= FeathersServerConfig.REST_STILL_TICKS.get()) {
            boolean crouching = entity.isCrouching();
            data.restState = crouching ? RestState.CROUCHING : RestState.STILL;
            multiplier = crouching ? FeathersServerConfig.REST_CROUCHING_MULTIPLIER.get() : FeathersServerConfig.REST_STILL_MULTIPLIER.get();
        } else {
            data.restState = RestState.NONE;
            multiplier = 1.0;
        }

        int size = data.restBonuses.size();
        for (int i = 0; i < size; i++) {
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
                        data.logSpend(drain.source, cost, now);
                    } else {
                        stop = DrainEvent.Stopped.Reason.INSUFFICIENT;
                        FeathersServiceImpl.checkExhausted(entity, data, strainEnabled);
                    }
                }
            }

            if (stop != null) {
                data.drains.remove(i);
                MinecraftForge.EVENT_BUS.post(new DrainEvent.Stopped(entity, drain.source, stop));
            }
        }
    }

    /**
     * Regeneration pays strain back first (faster while resting), then refills stamina. Fractions carry over, so
     * any rate works however slow.
     */
    private static void regenerate(LivingEntity entity, FeathersData data) {
        boolean blocked = data.isRegenBlocked();
        if (data.regenDelay > 0) data.regenDelay--;

        AttributeInstance attribute = data.regenAttribute(entity);
        double perTick = attribute != null ? Stamina.perTick(attribute.getValue()) : 0.0;

        if (perTick > 0) {
            boolean hungry = entity instanceof Player player && FeathersServerConfig.REGEN_USES_HUNGER.get()
                    && player.getFoodData().getFoodLevel() <= HungerRegen.HUNGRY_FOOD_LEVEL;
            if (blocked || hungry) {
                data.regenCarry = 0;
                data.regenPaused = true;
                return;
            }
            if (data.regenPaused && data.stamina < data.maxStamina) {
                if (MinecraftForge.EVENT_BUS.post(new RegenEvent(entity))) return;
                data.regenPaused = false;
            }
            if (FeathersServerConfig.REST_BOOSTS_REGEN.get()) perTick *= data.restMultiplier;
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
            // Resting accelerates strain recovery. Do not apply the multiplier twice after it boosts regeneration.
            double rest = FeathersServerConfig.REST_BOOSTS_REGEN.get() ? 1.0 : data.restMultiplier;
            int recovery = (int) Math.round(regen * rest);
            recovered = Math.min(data.strain, recovery);
            data.strain -= recovered;
            regen = (int) ((recovery - recovered) / rest);
        }

        int gained = Math.min(regen, Math.max(0, data.maxStamina - data.stamina));
        data.stamina += gained;
        data.totalRegenerated += gained + recovered;

        if (entity instanceof Player player && FeathersServerConfig.REGEN_USES_HUNGER.get() && gained + recovered > 0) {
            player.causeFoodExhaustion((float) ((gained + recovered) / (double) Stamina.PER_FEATHER * FeathersServerConfig.HUNGER_PER_FEATHER.get()));
        }
    }

    /**
     * Ends exhaustion once there is no strain left and the share of the bar set in the config is back, or at once
     * when exhaustion is turned off.
     */
    static void checkRecovered(LivingEntity entity, FeathersData data) {
        // Without a bar there is nothing to run out of or get back: no event pair every tick.
        if (data.maxStamina <= 0) {
            data.exhausted = false;
            return;
        }
        if (!data.exhausted) return;
        boolean recovered = !FeathersServerConfig.ENABLE_EXHAUSTION.get()
                || data.strain == 0 && data.availableStamina() >= usableMax(data) * FeathersServerConfig.EXHAUSTION_RECOVERY.get();
        if (recovered) {
            data.exhausted = false;
            MinecraftForge.EVENT_BUS.post(new ExhaustionEvent.Recovered(entity));
        }
    }

    /** The part of the bar armor weight leaves usable: recovery is measured against it, or heavy armor never recovers. */
    private static int usableMax(FeathersData data) {
        return Math.max(0, data.maxStamina - Stamina.ofFeathers(data.weight));
    }

    /**
     * The strained effect appears during strain recovery. Changing the effect sends a packet, so this method updates
     * it only when the state changes.
     */
    private static void updateIndicators(LivingEntity entity, FeathersData data, boolean strained) {
        if (strained == entity.hasEffect(FeathersMobEffects.STRAINED.get())) return;
        if (strained) {
            entity.addEffect(new MobEffectInstance(FeathersMobEffects.STRAINED.get(), FeathersMobEffect.PERMANENT, 0, false, false, true));
        } else {
            entity.removeEffect(FeathersMobEffects.STRAINED.get());
        }
    }

    /**
     * Undoes what the feathers left on a creature that stopped being a mount (mounts turned off, its type left the
     * mounts tag or data map): strain, exhaustion, the slowdown flag, the strained effect this mod put on it, and the
     * rider's HUD row. Its stamina and rolled trait remain available if it becomes a mount again.
     * Only creatures that had feathers are touched, and only once. Public for tests.
     */
    public static void releaseExMount(LivingEntity entity) {
        FeathersData data = FeathersServiceImpl.dataOrNull(entity);
        if (data == null) return;
        MobEffectInstance effect = entity.getEffect(FeathersMobEffects.STRAINED.get());
        // The built-in instance is permanent and has no particles. Preserve instances from commands or other mods.
        boolean ownEffect = effect != null && FeathersMobEffect.isPermanent(effect) && !effect.isVisible();
        if (!ownEffect && !data.initialized && data.strain == 0 && !data.exhausted && !data.mountSlowed && data.syncedMax <= 0) return;

        if (ownEffect) entity.removeEffect(FeathersMobEffects.STRAINED.get());
        if (data.strain > 0) {
            data.strain = 0;
            MinecraftForge.EVENT_BUS.post(new StrainEvent.Cleared(entity));
        }
        if (data.exhausted) {
            data.exhausted = false;
            MinecraftForge.EVENT_BUS.post(new ExhaustionEvent.Recovered(entity));
        }
        data.mountSlowed = false;
        data.drains.clear();
        // Becoming a mount again reads the config and attributes afresh (see tick).
        data.initialized = false;
        if (data.syncedMax > 0 && MountExertion.riderOf(entity) instanceof ServerPlayer rider) FeathersNetwork.sendNoFeathers(rider, entity);
        data.syncedMax = -1;
        data.forceSync = true;
    }

    /**
     * Sends the client a snapshot when something it shows changed: whole feathers, strain, bonus, weight, maximums,
     * exhaustion, the regeneration pause, rest.
     */
    private static void syncIfChanged(LivingEntity entity, FeathersData data) {
        // A player receives their own state. A mount sends its state to the controlling rider.
        ServerPlayer player = entity instanceof ServerPlayer self ? self
                : MountExertion.riderOf(entity) instanceof ServerPlayer rider ? rider : null;
        if (player == null) return;

        int shownStamina = FeathersServerConfig.DEBUG_MODE.get() ? data.stamina : Stamina.toFeathers(data.stamina);
        int shownStrain = Stamina.toFeathersCeil(data.strain);
        int shownBonus = Stamina.toFeathersCeil(data.bonusStamina());
        boolean delayed = data.regenDelay > 0;

        if (!data.forceSync && shownStamina == data.syncedStamina && data.maxStamina == data.syncedMax
                && shownStrain == data.syncedStrain && data.maxStrain == data.syncedMaxStrain && shownBonus == data.syncedBonus
                && data.weight == data.syncedWeight && data.exhausted == data.syncedExhausted && delayed == data.syncedDelayed
                && data.restState == data.syncedRest && data.weightSplit.sameAs(data.syncedWeightSplit)) return;

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
        data.syncedWeightSplit.copyFrom(data.weightSplit);
        FeathersNetwork.sendSync(player, entity, data);
    }
}
