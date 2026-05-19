package com.darkona.feathers.api;

import com.darkona.feathers.api.client.ClientFeathers;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.api.spi.FeathersService;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Green Feathers: a stamina bar of feathers that any mod can spend.
 * <p>
 * <b>Sides.</b> Everything that changes feathers is server side; the server syncs the owning player's client. On
 * the client, read the local player through {@link ClientFeathers}.
 * <p>
 * <b>Units.</b> Amounts are stamina, a thousandth of a feather (see {@link Stamina}). Costs therefore can be
 * fractions of a feather.
 * <p>
 * <b>Entities.</b> Methods take any {@link LivingEntity}; today only players have feathers. For others,
 * {@link #hasFeathers} is false, spending returns {@link SpendResult#EXEMPT} and views read zero.
 * <p>
 * <b>Sources.</b> Every spend, drain, bonus and block names its source with a {@link ResourceLocation} of your mod,
 * e.g. {@code mymod:dash}. Using the same source again replaces or refreshes your own entry and never touches
 * another mod's.
 * <p>
 * <b>Soft dependency.</b> Compile against {@code greenfeathers-api} only and guard calls with
 * {@code ModList.get().isLoaded("greenfeathers")}.
 */
public final class FeathersAPI {

    /** Bumped when the API changes incompatibly. */
    public static final int API_VERSION = 2;

    private static FeathersService service;

    private FeathersAPI() {}

    private static FeathersService service() {
        return Objects.requireNonNull(service, "Green Feathers is not loaded");
    }

    /* Reading */

    /**
     * Whether the entity uses feathers at all.
     */
    public static boolean hasFeathers(LivingEntity entity) {
        return service().supports(entity);
    }

    /**
     * A live read-only view of the entity's feathers. Cheap: no copy is made.
     */
    public static FeathersView get(LivingEntity entity) {
        return service().view(entity);
    }

    /* Spending */

    /**
     * Spends stamina once, all or nothing. Bonus stamina goes first, then regular stamina, then Strain if the
     * options and the server allow it. The usage multiplier attribute and stamina modifiers apply first.
     */
    public static SpendResult spend(LivingEntity entity, ResourceLocation source, int stamina, SpendOptions options) {
        return service().spend(entity, source, stamina, options);
    }

    public static SpendResult spend(LivingEntity entity, ResourceLocation source, int stamina) {
        return spend(entity, source, stamina, SpendOptions.DEFAULT);
    }

    public static SpendResult spendFeathers(LivingEntity entity, ResourceLocation source, int feathers) {
        return spend(entity, source, Stamina.ofFeathers(feathers), SpendOptions.DEFAULT);
    }

    /**
     * Whether a spend would go through, without spending or firing events.
     */
    public static boolean canSpend(LivingEntity entity, ResourceLocation source, int stamina, SpendOptions options) {
        return spend(entity, source, stamina, options.simulated()).allowed();
    }

    public static boolean canSpend(LivingEntity entity, ResourceLocation source, int stamina) {
        return canSpend(entity, source, stamina, SpendOptions.DEFAULT);
    }

    /**
     * Starts or refreshes a continuous drain, e.g. while sprinting or gliding. Costs go through the same rules as
     * {@link #spend}; fractions carry over between ticks. With a timeout (the default), call it every tick while
     * the activity lasts; otherwise stop it with {@link #stopDrain}.
     *
     * @return the result of this tick's payment; the drain stops by itself on anything but OK or EXEMPT
     */
    public static SpendResult startDrain(LivingEntity entity, ResourceLocation source, double staminaPerTick, DrainOptions options) {
        return service().startDrain(entity, source, staminaPerTick, options);
    }

    public static SpendResult startDrain(LivingEntity entity, ResourceLocation source, double staminaPerTick) {
        return startDrain(entity, source, staminaPerTick, DrainOptions.DEFAULT);
    }

    public static void stopDrain(LivingEntity entity, ResourceLocation source) {
        service().stopDrain(entity, source);
    }

    public static boolean isDraining(LivingEntity entity, ResourceLocation source) {
        return service().isDraining(entity, source);
    }

    /* Giving */

    /**
     * Gives stamina, up to the maximum.
     *
     * @return the stamina actually gained
     */
    public static int gain(LivingEntity entity, ResourceLocation source, int stamina) {
        return service().gain(entity, source, stamina);
    }

    /**
     * Temporary stamina spent before regular stamina, like the Endurance effect. Replaces any bonus of the same
     * source. Saved with the entity until it runs out or expires.
     *
     * @param ticks how long it lasts; negative for until spent or removed
     */
    public static void addBonusStamina(LivingEntity entity, ResourceLocation source, int stamina, int ticks) {
        service().addBonusStamina(entity, source, stamina, ticks);
    }

    public static void removeBonusStamina(LivingEntity entity, ResourceLocation source) {
        service().removeBonusStamina(entity, source);
    }

    /**
     * Sets the stamina directly, clamped to the maximum. For commands and scripted events; gameplay should spend
     * or gain.
     */
    public static void setStamina(LivingEntity entity, int stamina) {
        service().setStamina(entity, stamina);
    }

    /**
     * Full feathers, no Strain, not exhausted.
     */
    public static void reset(LivingEntity entity) {
        service().reset(entity);
    }

    /* Regeneration and rest */

    /**
     * Pauses regeneration for a while, e.g. during an action. Keyed by source, so blocks from different mods don't
     * undo each other.
     *
     * @param ticks negative for until {@link #unblockRegen}
     */
    public static void blockRegen(LivingEntity entity, ResourceLocation source, int ticks) {
        service().blockRegen(entity, source, ticks);
    }

    public static void unblockRegen(LivingEntity entity, ResourceLocation source) {
        service().unblockRegen(entity, source);
    }

    /**
     * Helps the entity rest, e.g. a hot spring: multiplies Strain recovery. The best multiplier among bonuses and
     * the entity's own rest state wins; they don't stack. Not saved: keep refreshing it while the cause lasts.
     */
    public static void setRestBonus(LivingEntity entity, ResourceLocation source, double multiplier, int ticks) {
        service().setRestBonus(entity, source, multiplier, ticks);
    }

    public static void removeRestBonus(LivingEntity entity, ResourceLocation source) {
        service().removeRestBonus(entity, source);
    }

    /* Climate and weight */

    /**
     * The entity's climate as last evaluated by the {@link ClimateProvider}s.
     */
    public static Climate getClimate(LivingEntity entity) {
        return service().getClimate(entity);
    }

    /**
     * The entity's current armor weight, in feathers.
     */
    public static int getArmorWeight(LivingEntity entity) {
        return service().getArmorWeight(entity);
    }

    /**
     * What one item weighs when worn, with its enchantments, in feathers. Fractional; totals are rounded once.
     */
    public static double getPieceWeight(ItemStack stack) {
        return service().getPieceWeight(stack);
    }

    /**
     * Recalculates the armor weight now, e.g. after your {@link WeightSource} changed its mind.
     */
    public static void recalculateWeight(LivingEntity entity) {
        service().recalculateWeight(entity);
    }

    /* Extension points. Register during mod construction or common setup. */

    public static void registerClimateProvider(ResourceLocation id, int priority, ClimateProvider provider) {
        service().registerClimateProvider(id, priority, provider);
    }

    public static void registerRegenFactor(ResourceLocation id, RegenFactor factor) {
        service().registerRegenFactor(id, factor);
    }

    public static void registerWeightSource(ResourceLocation id, WeightSource source) {
        service().registerWeightSource(id, source);
    }

    public static void registerStaminaModifier(ResourceLocation id, int ordinal, StaminaModifier modifier) {
        service().registerStaminaModifier(id, ordinal, modifier);
    }

    /* Data maps: see FeathersDataMaps. */

    public static @Nullable Integer dataMapArmorWeight(Item item) {
        return service().dataMapArmorWeight(item);
    }

    public static @Nullable MountStats dataMapMountStats(EntityType<?> type) {
        return service().dataMapMountStats(type);
    }

    /**
     * Sends the entity's feathers to its client now. Rarely needed: changes are synced automatically.
     */
    public static void sync(LivingEntity entity) {
        service().sync(entity);
    }

    /* Attributes and effects: plain vanilla calls, gathered here for convenience. */

    public static void setMaxFeathers(LivingEntity entity, double feathers) {
        AttributeInstance attr = entity.getAttribute(FeathersAttributes.MAX_FEATHERS.get());
        if (attr != null) attr.setBaseValue(feathers);
    }

    public static double getRegenPerSecond(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
        return attr != null ? attr.getValue() : 0.0;
    }

    public static void setBaseRegenPerSecond(LivingEntity entity, double feathersPerSecond) {
        AttributeInstance attr = entity.getAttribute(FeathersAttributes.FEATHERS_PER_SECOND.get());
        if (attr != null) attr.setBaseValue(feathersPerSecond);
    }

    public static double getUsageMultiplier(LivingEntity entity) {
        AttributeInstance attr = entity.getAttribute(FeathersAttributes.USAGE_MULTIPLIER.get());
        return attr != null ? attr.getValue() : 1.0;
    }

    public static boolean isCold(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.COLD.get());
    }

    public static boolean isHot(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.HOT.get());
    }

    public static boolean isFatigued(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.FATIGUE.get());
    }

    public static boolean isEnergized(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.ENERGIZED.get());
    }

    public static boolean isEnduring(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.ENDURANCE.get());
    }

    public static boolean hasMomentum(LivingEntity entity) {
        return entity.hasEffect(FeathersMobEffects.MOMENTUM.get());
    }

    /**
     * Internal: Green Feathers installs its implementation here.
     */
    public static void setService(FeathersService implementation) {
        if (service != null) throw new IllegalStateException("The Green Feathers service is already set");
        service = implementation;
    }
}
