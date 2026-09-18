package com.darkona.feathersoffatigue;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.registry.FeathersIds;
import com.darkona.feathersoffatigue.climate.ClimateEffects;
import com.darkona.feathersoffatigue.compatibility.coldsweat.ColdSweatCompat;
import com.darkona.feathersoffatigue.compatibility.curios.CuriosCompat;
import com.darkona.feathersoffatigue.compatibility.dropletsofthirst.DropletsOfThirstCompat;
import com.darkona.feathersoffatigue.compatibility.lso.LegendarySurvivalCompat;
import com.darkona.feathersoffatigue.compatibility.sereneseasons.SereneSeasonsCompat;
import com.darkona.feathersoffatigue.compatibility.thirst.ThirstCompat;
import com.darkona.feathersoffatigue.compatibility.toughasnails.ToughAsNailsCompat;
import com.darkona.feathersoffatigue.config.FeathersClientConfig;
import com.darkona.feathersoffatigue.config.FeathersCompatConfig;
import com.darkona.feathersoffatigue.config.FeathersServerConfig;
import com.darkona.feathersoffatigue.core.FeathersAttachments;
import com.darkona.feathersoffatigue.core.FeathersServiceImpl;
import com.darkona.feathersoffatigue.core.FeathersTicker;
import com.darkona.feathersoffatigue.core.HungerRegen;
import com.darkona.feathersoffatigue.data.DataMaps;
import com.darkona.feathersoffatigue.effect.ModEffects;
import com.darkona.feathersoffatigue.item.FeatherRingItem;
import com.darkona.feathersoffatigue.item.ModItems;
import com.darkona.feathersoffatigue.network.FeathersNetwork;
import com.darkona.feathersoffatigue.registry.ModAttributes;
import com.darkona.feathersoffatigue.registry.ModEnchantments;
import com.darkona.feathersoffatigue.registry.ModPotions;
import com.darkona.feathersoffatigue.style.BuiltInFeatherStyles;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(FeathersIds.MOD_ID)
public final class Feathers {

    public static final Logger LOGGER = LoggerFactory.getLogger(FeathersIds.MOD_ID);

    public Feathers() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        FeathersAPI.setService(FeathersServiceImpl.INSTANCE);

        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.SERVER, FeathersServerConfig.SPEC, "feathers_of_fatigue/FeathersOfFatigue-Server.toml");
        context.registerConfig(ModConfig.Type.SERVER, FeathersCompatConfig.SPEC, "feathers_of_fatigue/FeathersOfFatigue-Compat.toml");
        context.registerConfig(ModConfig.Type.CLIENT, FeathersClientConfig.SPEC, "feathers_of_fatigue/FeathersOfFatigue-Client.toml");

        ModAttributes.register(modEventBus);
        ModEffects.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModPotions.register(modEventBus);
        ModItems.register(modEventBus);
        FeathersAttachments.register(modEventBus);

        modEventBus.addListener(FeathersTicker::addAttributes);
        modEventBus.addListener(FeathersTicker::onConfigChanged);
        modEventBus.addListener(Feathers::commonSetup);
        FeathersNetwork.register();

        // The data maps: loaded with the server data, sent to each client when it joins and after every reload.
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new DataMaps.Loader(event.getConditionContext())));
        MinecraftForge.EVENT_BUS.addListener((OnDatapackSyncEvent event) -> event.getPlayers().forEach(FeathersNetwork::sendDataMaps));

        // Built-in extensions go through the same API as other mods' do.
        ClimateEffects.registerBuiltIn();
        HungerRegen.registerBuiltIn();
        BuiltInFeatherStyles.register();
        ColdSweatCompat.init();
        ThirstCompat.init();
        DropletsOfThirstCompat.init();
        ToughAsNailsCompat.init();
        LegendarySurvivalCompat.init();
        SereneSeasonsCompat.init();
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModEffects.bindAttributeModifiers();
            ModPotions.registerBrewingRecipes();
            if (FeatherRingItem.CURIOS) CuriosCompat.init();
        });
    }
}
