package com.darkona.feathers.effect;

import com.darkona.feathers.api.registry.FeathersMobEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.darkona.feathers.core.FeathersTicker.modifierId;

/**
 * Base of the feathers effects. {@link #canApply} is binding: {@link EffectEvents} refuses the effect otherwise.
 */
public class FeathersMobEffect extends MobEffect {

    /**
     * 1.19.2 has no infinite effects: a permanent one lasts this long (years), and the inventory shows it as "**:**".
     */
    public static final int PERMANENT = Integer.MAX_VALUE;

    /** Past any duration a command or potion can give (1,000,000 seconds). */
    private static final int PERMANENT_THRESHOLD = Integer.MAX_VALUE / 2;

    /**
     * Whether {@code instance} is one of the permanent effects, what later versions call an infinite duration.
     */
    public static boolean isPermanent(MobEffectInstance instance) {
        return instance.getDuration() > PERMANENT_THRESHOLD;
    }

    private record PendingModifier(Supplier<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {}

    /** The feathers attributes register after the effects: their modifiers are added once both exist. */
    private final List<PendingModifier> pending = new ArrayList<>(1);

    public FeathersMobEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    /**
     * An attribute modifier of the effect, times its level. Added in common setup, see {@link #bindModifiers}.
     */
    public FeathersMobEffect withModifier(Supplier<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        pending.add(new PendingModifier(attribute, id, amount, operation));
        return this;
    }

    void bindModifiers() {
        for (PendingModifier modifier : pending) {
            addAttributeModifier(modifier.attribute().get(), modifierId(modifier.id()).toString(), modifier.amount(), modifier.operation());
        }
        pending.clear();
    }

    /**
     * Whether the entity may have this effect now. Creative and invulnerable players take none.
     */
    public boolean canApply(LivingEntity entity) {
        return !(entity instanceof Player player && (player.isCreative() || player.getAbilities().invulnerable));
    }

    /**
     * The effect was added to an entity that {@link #canApply} accepted.
     *
     * @param previous the instance it replaces (a refresh or a stronger level), or null for a new effect
     */
    public void onApplied(LivingEntity entity, MobEffectInstance instance, @Nullable MobEffectInstance previous) {}

    /**
     * The effect was removed or expired.
     */
    public void onEnded(LivingEntity entity, MobEffectInstance instance) {}

    /**
     * Fire Resistance and Cooling protect from every heat effect.
     */
    public static boolean isProtectedFromHeat(LivingEntity entity) {
        return entity.hasEffect(MobEffects.FIRE_RESISTANCE) || entity.hasEffect(FeathersMobEffects.COOLING.get());
    }

}
