package com.elenai.feathers.effect;

import com.elenai.feathers.api.FeathersConstants;
import com.elenai.feathers.capability.PlayerFeathersProvider;
import com.elenai.feathers.networking.FeathersMessages;
import com.elenai.feathers.networking.packet.Effect;
import com.elenai.feathers.networking.packet.EffectChangeSTCPacket;
import com.elenai.feathers.networking.packet.FeatherSyncSTCPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class EnduranceEffect extends MobEffect {

    public EnduranceEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void addAttributeModifiers(LivingEntity target, AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setEnduranceStamina((strength + 1) * (8 * FeathersConstants.STAMINA_PER_FEATHER));
                FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENDURANCE, true, strength), player);
            });
        }
        super.addAttributeModifiers(target, map, strength);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity target, AttributeMap map, int strength) {
        if (target instanceof ServerPlayer player) {
            player.getCapability(PlayerFeathersProvider.PLAYER_FEATHERS).ifPresent(f -> {
                f.setEnduranceStamina(0);
                FeathersMessages.sendToPlayer(new EffectChangeSTCPacket(Effect.ENDURANCE, false, strength), player);
            });
        }
        super.removeAttributeModifiers(target, map, strength);
    }

}
