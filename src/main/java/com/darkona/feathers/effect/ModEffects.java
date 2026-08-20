package com.darkona.feathers.effect;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.compatibility.coldsweat.ColdSweatCompat;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersServiceImpl;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import static com.darkona.feathers.api.registry.FeathersAttributes.FEATHERS_PER_SECOND;
import static com.darkona.feathers.api.registry.FeathersAttributes.MAX_FEATHERS;
import static com.darkona.feathers.api.registry.FeathersAttributes.USAGE_MULTIPLIER;
import static com.darkona.feathers.api.registry.FeathersIds.id;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL;

/**
 * Registers the effects whose holders live in the API's FeathersMobEffects. Most are plain attribute modifiers.
 */
public final class ModEffects {

    /** Bonus stamina source of the Endurance effect. */
    public static final ResourceLocation ENDURANCE_BONUS = id("endurance_effect");

    /** Endurance's golden feathers: eight per level. */
    private static int enduranceBonus(MobEffectInstance instance) {
        return Stamina.ofFeathers((instance.getAmplifier() + 1) * 8);
    }

    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FeathersIds.MOD_ID);

    static {
        EFFECTS.register("endurance", () -> new FeathersMobEffect(MobEffectCategory.BENEFICIAL, 0xFFFF00) {
            @Override
            public boolean canApply(LivingEntity entity) {
                return super.canApply(entity) && FeathersServerConfig.ENABLE_ENDURANCE.get();
            }

            /**
             * The golden feathers, for as long as the effect lasts or until spent. A refresh extends what
             * is left of them, and a stronger level adds its extra feathers to it: a potion never refills the pool.
             */
            @Override
            public void onApplied(LivingEntity entity, MobEffectInstance instance, @Nullable MobEffectInstance previous) {
                if (!FeathersAPI.hasFeathers(entity)) return;
                int full = enduranceBonus(instance);
                int left = FeathersServiceImpl.data(entity).bonusStamina(ENDURANCE_BONUS);
                int amount = previous != null && left > 0 ? Math.min(full, left + Math.max(0, full - enduranceBonus(previous))) : full;
                FeathersAPI.addBonusStamina(entity, ENDURANCE_BONUS, amount, FeathersMobEffect.isPermanent(instance) ? -1 : instance.getDuration());
            }


            @Override
            public void onEnded(LivingEntity entity, MobEffectInstance instance) {
                FeathersAPI.removeBonusStamina(entity, ENDURANCE_BONUS);
            }
        });

        EFFECTS.register("cold", () -> new FeathersMobEffect(MobEffectCategory.HARMFUL, 0xB6FFFF) {
            @Override
            public boolean canApply(LivingEntity entity) {
                if (!super.canApply(entity) || !FeathersServerConfig.ENABLE_COLD.get()) return false;
                if (!ColdSweatCompat.canApplyCold(entity)) return false;
                // Cold and Hot never coexist. Climate replaces one with the other, and potions cannot stack them.
                return !entity.hasEffect(FeathersMobEffects.ENERGIZED.get()) && !entity.hasEffect(FeathersMobEffects.HOT.get());
            }
        }.withModifier(FEATHERS_PER_SECOND, id("effect.cold"), -0.5, MULTIPLY_TOTAL));

        EFFECTS.register("energized", () -> new FeathersMobEffect(MobEffectCategory.BENEFICIAL, 0x71CEFF)
                .withModifier(FEATHERS_PER_SECOND, id("effect.energized"), 1.0, MULTIPLY_TOTAL));

        EFFECTS.register("hot", () -> new FeathersMobEffect(MobEffectCategory.HARMFUL, 0x7E5D48) {
            @Override
            public boolean canApply(LivingEntity entity) {
                if (!super.canApply(entity) || !FeathersServerConfig.ENABLE_HEAT.get() || isProtectedFromHeat(entity)) return false;
                if (!ColdSweatCompat.canApplyHeat(entity)) return false;
                return !entity.hasEffect(FeathersMobEffects.MOMENTUM.get()) && !entity.hasEffect(FeathersMobEffects.COLD.get());
            }
        }.withModifier(USAGE_MULTIPLIER, id("effect.hot"), 1.0, ADDITION));

        EFFECTS.register("fatigued", () -> new FeathersMobEffect(MobEffectCategory.HARMFUL, 0xFF0048) {
            @Override
            public boolean canApply(LivingEntity entity) {
                return super.canApply(entity) && FeathersServerConfig.ENABLE_FATIGUE.get() && !isProtectedFromHeat(entity);
            }
        }.withModifier(MAX_FEATHERS, id("effect.fatigued"), -4.0, ADDITION));

        EFFECTS.register("momentum", () -> new FeathersMobEffect(MobEffectCategory.BENEFICIAL, 0x7E5684) {
            @Override
            public boolean canApply(LivingEntity entity) {
                return super.canApply(entity) && FeathersServerConfig.ENABLE_MOMENTUM.get();
            }

            // Half the cost at level I, 35% at II, never below 20%: never free.
            @Override
            public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
                return Math.max(-0.8, -0.5 - 0.15 * amplifier);
            }
        }.withModifier(USAGE_MULTIPLIER, id("effect.momentum"), -0.5, MULTIPLY_TOTAL));

        // This effect appears during strain recovery. Its regeneration penalty makes strain costly.
        EFFECTS.register("strain", () -> new FeathersMobEffect(MobEffectCategory.HARMFUL, 0x7E4488) {
            @Override
            public boolean canApply(LivingEntity entity) {
                return super.canApply(entity) && FeathersServerConfig.ENABLE_STRAIN.get();
            }
        }.withModifier(FEATHERS_PER_SECOND, id("effect.strain"), -0.75, MULTIPLY_TOTAL));

        EFFECTS.register("cooling", () -> new FeathersMobEffect(MobEffectCategory.BENEFICIAL, 0x5FE0B4));
    }

    public static void register(IEventBus modEventBus) {
        EFFECTS.register(modEventBus);
    }

    /**
     * Adds the effects' attribute modifiers, now that the attributes exist. Common setup.
     */
    public static void bindAttributeModifiers() {
        for (RegistryObject<MobEffect> effect : EFFECTS.getEntries()) {
            if (effect.get() instanceof FeathersMobEffect feathers) feathers.bindModifiers();
        }
    }

    private ModEffects() {}
}
