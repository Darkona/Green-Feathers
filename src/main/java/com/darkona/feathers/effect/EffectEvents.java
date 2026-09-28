package com.darkona.feathers.effect;

import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

@EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class EffectEvents {

    private EffectEvents() {}

    /**
     * Makes {@link FeathersMobEffect#canApply} binding: an effect the entity can't take is refused, whether it comes
     * from the climate, a potion or another mod.
     */
    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (event.getEffectInstance().getEffect().value() instanceof FeathersMobEffect effect && !effect.canApply(event.getEntity())) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
    }

    @SubscribeEvent
    public static void onAdded(MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect().value() instanceof FeathersMobEffect effect && !event.getEntity().level().isClientSide()) {
            effect.onApplied(event.getEntity(), event.getEffectInstance());
        }
    }

    @SubscribeEvent
    public static void onRemoved(MobEffectEvent.Remove event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    @SubscribeEvent
    public static void onExpired(MobEffectEvent.Expired event) {
        ended(event.getEntity(), event.getEffectInstance());
    }

    private static void ended(LivingEntity entity, MobEffectInstance instance) {
        if (instance != null && instance.getEffect().value() instanceof FeathersMobEffect effect && !entity.level().isClientSide()) {
            effect.onEnded(entity, instance);
        }
    }
}
