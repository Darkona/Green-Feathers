package com.darkona.feathers.effect.effects;

import com.darkona.feathers.compatibility.coldsweat.ColdSweatManager;
import com.darkona.feathers.compatibility.coldsweat.FeathersColdSweatConfig;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.effect.FeathersEffects;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import static com.darkona.feathers.attributes.FeathersAttributes.STAMINA_USAGE_MULTIPLIER;
import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

public class HotEffect extends FeathersEffects {

    public static final double BASE_STRENGTH = 1.0d;
    /**
     * Doubles the amount of feathers used.
     */

    private static final String MODIFIER_UUID = "2a513b45-8047-472b-b5f1-c833440c0134";


    public HotEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
        addAttributeModifier(STAMINA_USAGE_MULTIPLIER.get(), MODIFIER_UUID, BASE_STRENGTH, ADDITION);
    }

    @Override
    public boolean canApply(Player player) {
        if (super.canApply(player) && FeathersCommonConfig.ENABLE_HEAT.get()) {
            if (FeathersColdSweatConfig.isColdSweatEnabled()) {
                return ColdSweatManager.canApplyHotEffect(player);
            }
            return !(player.hasEffect(MobEffects.FIRE_RESISTANCE) || player.hasEffect(FeathersEffects.MOMENTUM.get()));
        }
        return false;
    }


}
