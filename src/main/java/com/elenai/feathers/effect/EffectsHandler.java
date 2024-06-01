package com.elenai.feathers.effect;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.api.ICapabilityPlugin;
import com.elenai.feathers.capability.Capabilities;
import com.elenai.feathers.compatibility.coldsweat.ColdSweatManager;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.elenai.feathers.effect.effects.FeathersEffects;
import com.elenai.feathers.event.FeatherAmountEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Feathers.MODID)
public class EffectsHandler implements ICapabilityPlugin {

    private static ICapabilityPlugin instance;

    public static ICapabilityPlugin getInstance() {
        if (instance == null) {
            instance = new EffectsHandler();
        }
        return instance;
    }

    @SubscribeEvent
    public static void canApplyEffect(MobEffectEvent.Applicable event) {

        if (event.getEntity() instanceof Player player &&
                event.getEffectInstance().getEffect() instanceof FeathersEffects effect) {

            if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get() ||
                    player.getAbilities().invulnerable ||
                    player.isCreative()) return;

            if (effect == FeathersEffects.HOT.get()) {
                event.setResult(canApplyHotEffect(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }

            if (effect == FeathersEffects.COLD.get()) {
                event.setResult(canApplyColdEffect(player) ? Event.Result.ALLOW : Event.Result.DENY);
            }
        }
    }

    @SubscribeEvent
    public static void onFeathersEmpty(FeatherAmountEvent.Empty event) {
        event.getEntity().getCapability(Capabilities.PLAYER_FEATHERS).ifPresent(f -> {

            if (FeathersCommonConfig.ENABLE_STRAIN.get()) {

                if (event.prevStamina > 0) {

                    event.getEntity().addEffect(new MobEffectInstance(FeathersEffects.STRAINED.get()));
                } else {

                    event.getEntity().removeEffect(FeathersEffects.STRAINED.get());
                }
            }
        });
    }


    public static boolean canApplyColdEffect(Player player) {


        if (FeathersColdSweatConfig.isColdSweatEnabled()) {
            return ColdSweatManager.canApplyColdEffect(player);
        }

        return !player.hasEffect(FeathersEffects.ENERGIZED.get());
    }

    public static boolean canApplyHotEffect(Player player) {

        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() ||
                player.getAbilities().invulnerable ||
                player.isCreative()) return false;


        if (FeathersColdSweatConfig.isColdSweatEnabled()) {
            return ColdSweatManager.canApplyHotEffect(player);
        }

        return !player.hasEffect(MobEffects.FIRE_RESISTANCE);
    }


    /**
     * Handle the cold mechanic here.
     */
    public static void autoApplyColdEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get()) return;

        var coldEffect = FeathersEffects.COLD.get();
        var hasCold = player.hasEffect(coldEffect);
        var coldDuration = hasCold ? player.getActiveEffectsMap().get(coldEffect).getDuration() : 0;
        var coldLingerDuration = FeathersCommonConfig.COLD_LINGER.get();

        var hotEffect = FeathersEffects.HOT.get();
        var hasHot = player.hasEffect(hotEffect);
        var hotDuration = hasHot ? player.getActiveEffectsMap().get(hotEffect).getDuration() : 0;

        if (player.isCreative() && player.hasEffect(coldEffect)) {
            player.removeEffect(coldEffect);
            return;
        }

        if (isInColdSituation(player)) {

            if (hotDuration != 0) {

                player.removeEffect(hotEffect);
            } else if (!hasCold) {

                player.addEffect(new MobEffectInstance(coldEffect, -1, 0, false, true));
            }

        } else if (coldDuration > coldLingerDuration || coldDuration == -1) {

            player.removeEffect(coldEffect);
            player.addEffect(new MobEffectInstance(coldEffect, coldLingerDuration, 0, false, true));
        }
    }


    /**
     * Handles the hot mechanic here.
     */
    public static void autoApplyHotEffect(Player player) {
        if (!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get()) return;

        var coldEffect = FeathersEffects.COLD.get();
        var hasCold = player.hasEffect(coldEffect);
        var coldDuration = hasCold ? player.getActiveEffectsMap().get(coldEffect).getDuration() : 0;

        var hotEffect = FeathersEffects.HOT.get();
        var hasHot = player.hasEffect(hotEffect);
        var hotDuration = hasHot ? player.getActiveEffectsMap().get(hotEffect).getDuration() : 0;

        if (player.isCreative() && hasHot) {
            player.removeEffect(hotEffect);
            return;
        }

        if (isInHotSituation(player)) {

            if (coldDuration != 0) {

                player.removeEffect(coldEffect);

            } else if (!hasHot) {

                player.addEffect(new MobEffectInstance(hotEffect, -1, 0, false, true));
            }

        } else if (hotDuration > FeathersCommonConfig.COLD_LINGER.get() || hotDuration == -1) {

            player.removeEffect(hotEffect);
            player.addEffect(new MobEffectInstance(hotEffect, FeathersCommonConfig.COLD_LINGER.get(), 0, false, true));
        }
    }


    public static boolean isInColdSituation(Player player) {



        if (FeathersColdSweatConfig.isColdSweatEnabled() && FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {
            return ColdSweatManager.isFreezing(player);
        }

        boolean isExposedToWeather = player.level().isRaining() && player.level().isRainingAt(player.blockPosition()) &&
                player.level().canSeeSky(player.blockPosition());

        boolean isInColdBiome = player.level().getBiome(player.blockPosition()).get().coldEnoughToSnow(player.blockPosition());

        return (isInColdBiome && isExposedToWeather ) || player.isFreezing();
    }


    public static boolean isInHotSituation(Player player) {

        boolean isBurning = player.wasOnFire || player.isOnFire() || player.isInLava();

        if (FeathersColdSweatConfig.isColdSweatEnabled() && FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()) {
            return ColdSweatManager.isOverheating(player) || isBurning;
        }

        boolean isInHotBiome = player.level().getBiome(player.blockPosition()).get().getModifiedClimateSettings().temperature() > 0.45f ||
                player.level().dimension().equals(Level.NETHER);

        return isBurning || isInHotBiome;
    }


    @Override
    public void onPlayerTickBefore(TickEvent.PlayerTickEvent event) {

        autoApplyColdEffect(event.player);

        autoApplyHotEffect(event.player);

    }

    @Override
    public void onPlayerTickAfter(TickEvent.PlayerTickEvent event) {

    }

    @Override
    public void onPlayerJoin(EntityJoinLevelEvent event) {

    }
}
