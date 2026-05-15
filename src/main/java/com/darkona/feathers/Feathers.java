package com.darkona.feathers;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersDataMaps;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.climate.ClimateEffects;
import com.darkona.feathers.compatibility.coldsweat.ColdSweatCompat;
import com.darkona.feathers.compatibility.curios.CuriosCompat;
import com.darkona.feathers.compatibility.lso.LegendarySurvivalCompat;
import com.darkona.feathers.compatibility.sereneseasons.SereneSeasonsCompat;
import com.darkona.feathers.compatibility.thirst.ThirstCompat;
import com.darkona.feathers.compatibility.toughasnails.ToughAsNailsCompat;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.config.FeathersCompatConfig;
import com.darkona.feathers.core.FeathersAttachments;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.core.FeathersTicker;
import com.darkona.feathers.core.HungerRegen;
import com.darkona.feathers.effect.ModEffects;
import com.darkona.feathers.item.ModItems;
import com.darkona.feathers.network.FeathersNetwork;
import com.darkona.feathers.registry.ModAttributes;
import com.darkona.feathers.registry.ModPotions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FeathersIds.MOD_ID)
public final class Feathers {

    public static final Logger LOGGER = LoggerFactory.getLogger(FeathersIds.MOD_ID);

    public Feathers(IEventBus modEventBus, ModContainer modContainer) {
        FeathersAPI.setService(FeathersServiceImpl.INSTANCE);

        modContainer.registerConfig(ModConfig.Type.SERVER, FeathersServerConfig.SPEC, "feathers/Feathers-Server.toml");
        modContainer.registerConfig(ModConfig.Type.SERVER, FeathersCompatConfig.SPEC, "feathers/Feathers-Compat.toml");
        modContainer.registerConfig(ModConfig.Type.CLIENT, FeathersClientConfig.SPEC, "feathers/Feathers-Client.toml");

        ModAttributes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModItems.register(modEventBus);
        FeathersAttachments.register(modEventBus);

        modEventBus.addListener(FeathersTicker::addAttributes);
        modEventBus.addListener(FeathersTicker::onConfigChanged);
        modEventBus.addListener(FeathersNetwork::register);
        modEventBus.addListener((RegisterDataMapTypesEvent event) -> {
            event.register(FeathersDataMaps.ARMOR_WEIGHT);
            event.register(FeathersDataMaps.MOUNT_STATS);
        });
        modEventBus.addListener(Feathers::commonSetup);

        // Built-in extensions go through the same API as other mods' do.
        ClimateEffects.registerBuiltIn();
        HungerRegen.registerBuiltIn();
        ColdSweatCompat.init();
        ThirstCompat.init();
        ToughAsNailsCompat.init();
        LegendarySurvivalCompat.init();
        SereneSeasonsCompat.init();
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        if (ModList.get().isLoaded("curios")) event.enqueueWork(CuriosCompat::init);
    }
}
