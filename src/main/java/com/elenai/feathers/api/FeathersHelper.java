package com.elenai.feathers.api;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.config.FeathersCommonConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FeathersHelper {

    @Deprecated
    public static boolean spendFeathers(int amount) {
        return spendFeathers(amount, FeathersCommonConfig.DEFAULT_USAGE_COOLDOWN.get());
    }

    @Deprecated
    public static boolean spendFeathers(int amount, int cooldown) {
        var player = Minecraft.getInstance().player;
        if (player == null) return false;
        return FeathersAPI.spendFeathers(player, amount, cooldown);
    }

}
