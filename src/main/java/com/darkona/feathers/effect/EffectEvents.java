package com.darkona.feathers.effect;

import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class EffectEvents {

    private EffectEvents() {}

    /**
     * Makes {@link FeathersMobEffect#canApply} binding: an effect the entity can't take is refused, whether it comes
     * from the climate, a potion or another mod.
     */
    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect() instanceof FeathersMobEffect effect && !effect.canApply(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        MobEffectInstance added = event.getEffectInstance(), old = event.getOldEffectInstance();
        // Fired before the merge: the entity keeps its current instance unless the new one is stronger, or as strong and
        // longer (MobEffectInstance.update), so a weaker potion mustn't replace what the stronger one gave.
        if (old != null && (added.getAmplifier() < old.getAmplifier()
                || added.getAmplifier() == old.getAmplifier() && added.getDuration() <= old.getDuration())) return;
        if (added.getEffect() instanceof FeathersMobEffect effect && !event.getEntity().level().isClientSide()) {
            effect.onApplied(event.getEntity(), added);
        }
    }

    /** Last: another mod may cancel the removal, and then the effect (and its bonus) stays. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRemoved(MobEffectEvent.Remove event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    private static void ended(LivingEntity entity, MobEffectInstance instance) {
        if (instance != null && instance.getEffect() instanceof FeathersMobEffect effect && !entity.level().isClientSide()) {
            effect.onEnded(entity, instance);
        }
    }
}
