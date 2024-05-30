package com.elenai.feathers.effect;

import com.elenai.feathers.Feathers;
import com.elenai.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.elenai.feathers.config.FeathersCommonConfig;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.util.registries.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class PlayerSituationProvider {

    public static boolean isInColdSituation(Player player){

        boolean isInSnow = player.isInPowderSnow || player.wasInPowderSnow;

        if(Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()){
            return Temperature.get(player, Temperature.Trait.BODY) <= -55;
        }

        boolean isInColdBiome = player.level().getBiome(player.blockPosition()).get().coldEnoughToSnow(player.blockPosition());

        return isInSnow || isInColdBiome;
    }


    public static boolean isInHotSituation(ServerPlayer player){

        boolean isBurning = player.wasOnFire || player.isOnFire() || player.isInLava();

        if(Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()){
            return Temperature.get(player, Temperature.Trait.BODY) >= 55;
        }

        boolean isInHotBiome = player.level().getBiome(player.blockPosition()).get().getModifiedClimateSettings().temperature() > 0.45f;

        return isBurning || isInHotBiome;
    }

    public static boolean canBeCold(ServerPlayer player){
        if(!FeathersCommonConfig.ENABLE_COLD_EFFECTS.get() || player.getAbilities().invulnerable || player.isCreative()) return false;

        if(Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_COLD_ADDS_COLD_EFFECT.get()) {
            if(player.hasEffect(ModEffects.GRACE) || player.hasEffect(ModEffects.ICE_RESISTANCE)){
                return false;
            }
        }

        return !player.hasEffect(FeathersEffects.ENERGIZED.get());
    }

    public static boolean canBeHot(ServerPlayer player){
        if(!FeathersCommonConfig.ENABLE_HOT_EFFECTS.get() || player.getAbilities().invulnerable || player.isCreative()) return false;
        boolean hasResistance = player.hasEffect(MobEffects.FIRE_RESISTANCE);
        if(Feathers.COLD_SWEAT_LOADED &&
                FeathersColdSweatConfig.COLD_SWEAT_COMPATIBILITY.get() &&
                FeathersColdSweatConfig.BEING_HOT_ADDS_HOT_EFFECT.get()){
            return !player.hasEffect(ModEffects.GRACE) && !hasResistance;
        }
        return !hasResistance;
    }
}
