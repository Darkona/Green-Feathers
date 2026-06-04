package com.darkona.feathers.compatibility.bluedroplets;

import com.darkona.droplets.api.DropletsView;
import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.compatibility.ThirstRegen;
import com.darkona.feathers.config.FeathersCompatConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Blue Droplets compatibility, safe to load without it: calls into the mod go through {@link BlueDropletsBridge}.
 */
public final class BlueDropletsCompat {

    private static final ThirstRegen.Keys KEYS = ThirstRegen.Keys.of("blue_droplets");

    public static final boolean LOADED = ModList.get().isLoaded("bluedroplets");

    private BlueDropletsCompat() {}

    public static void init() {
        if (LOADED) FeathersAPI.registerRegenFactor(id("blue_droplets"), BlueDropletsCompat::regenFactor);
    }

    private static double regenFactor(LivingEntity entity, FeathersView feathers) {
        if (!FeathersCompatConfig.BLUE_DROPLETS.get() || !(entity instanceof Player player)) return 0.0;

        DropletsView thirst = BlueDropletsBridge.view(player);
        if (!thirst.isEnabled()) return 0.0;

        int owed = ThirstRegen.owedSinceLastCall(player, feathers, KEYS, FeathersCompatConfig.BLUE_DROPLETS_THIRST_PER_FEATHER.get());
        if (owed > 0 && !player.level().isClientSide()) BlueDropletsBridge.drain(player, owed);

        return ThirstRegen.factor(thirst.thirst(), thirst.quenched(),
                FeathersCompatConfig.BLUE_DROPLETS_REGEN_REDUCTION.get(), FeathersCompatConfig.BLUE_DROPLETS_QUENCH_BONUS.get());
    }
}
