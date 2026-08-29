package com.darkona.feathers.effect;

import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class EffectEvents {

    private EffectEvents() {}

    /**
     * Enforces {@link FeathersMobEffect#canApply} for effects from climate, potions, and other mods.
     */
    @SubscribeEvent
    public static void onApplicable(PotionEvent.PotionApplicableEvent event) {
        if (event.getPotionEffect().getEffect() instanceof FeathersMobEffect effect && !effect.canApply(event.getEntityLiving())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onAdded(PotionEvent.PotionAddedEvent event) {
        MobEffectInstance added = event.getPotionEffect();
        MobEffectInstance old = event.getOldPotionEffect();
        // This event fires before the merge. Minecraft keeps the current instance unless the new one is stronger,
        // or has the same strength and a longer duration. A weaker potion must not replace the stronger bonus.
        if (old != null && (added.getAmplifier() < old.getAmplifier()
                || added.getAmplifier() == old.getAmplifier() && added.getDuration() <= old.getDuration())) return;
        if (added.getEffect() instanceof FeathersMobEffect effect && !event.getEntityLiving().level.isClientSide()) {
            effect.onApplied(event.getEntityLiving(), added, old);
        }
    }

    /** Last, so a mod that cancels the removal (keeping the effect) does it before the effect's end runs. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRemoved(PotionEvent.PotionRemoveEvent event) {
        ended(event.getEntityLiving(), event.getPotionEffect());
    }

    @SubscribeEvent
    public static void onExpired(PotionEvent.PotionExpiryEvent event) {
        ended(event.getEntityLiving(), event.getPotionEffect());
    }

    private static void ended(LivingEntity entity, MobEffectInstance instance) {
        if (instance != null && instance.getEffect() instanceof FeathersMobEffect effect && !entity.level.isClientSide()) {
            effect.onEnded(entity, instance);
        }
    }
}
