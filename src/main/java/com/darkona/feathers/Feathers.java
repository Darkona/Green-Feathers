package com.darkona.feathers;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.climate.ClimateEffects;
import com.darkona.feathers.compatibility.coldsweat.ColdSweatCompat;
import com.darkona.feathers.compatibility.curios.CuriosCompat;
import com.darkona.feathers.compatibility.sereneseasons.SereneSeasonsCompat;
import com.darkona.feathers.compatibility.thirst.ThirstCompat;
import com.darkona.feathers.compatibility.toughasnails.ToughAsNailsCompat;
import com.darkona.feathers.config.FeathersClientConfig;
import com.darkona.feathers.config.FeathersCompatConfig;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersAttachments;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.core.FeathersTicker;
import com.darkona.feathers.core.HungerRegen;
import com.darkona.feathers.data.DataMaps;
import com.darkona.feathers.effect.ModEffects;
import com.darkona.feathers.item.ModItems;
import com.darkona.feathers.network.FeathersNetwork;
import com.darkona.feathers.registry.ModAttributes;
import com.darkona.feathers.registry.ModEnchantments;
import com.darkona.feathers.registry.ModPotions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
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
        context.registerConfig(ModConfig.Type.SERVER, FeathersServerConfig.SPEC, "feathers/Feathers-Server.toml");
        context.registerConfig(ModConfig.Type.SERVER, FeathersCompatConfig.SPEC, "feathers/Feathers-Compat.toml");
        context.registerConfig(ModConfig.Type.CLIENT, FeathersClientConfig.SPEC, "feathers/Feathers-Client.toml");

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
        if (ModList.get().isLoaded("curios")) CuriosCompat.init(modEventBus);

        // The data maps: loaded with the server data, sent to each client when it joins and after every reload.
        MinecraftForge.EVENT_BUS.addListener((AddReloadListenerEvent event) -> event.addListener(new DataMaps.Loader(event.getConditionContext())));
        MinecraftForge.EVENT_BUS.addListener(Feathers::sendDataMaps);

        // Built-in extensions go through the same API as other mods' do.
        ClimateEffects.registerBuiltIn();
        HungerRegen.registerBuiltIn();
        ColdSweatCompat.init();
        ThirstCompat.init();
        ToughAsNailsCompat.init();
        SereneSeasonsCompat.init();
    }

    /** One player when one joins, everyone after a reload. */
    private static void sendDataMaps(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) FeathersNetwork.sendDataMaps(event.getPlayer());
        else event.getPlayerList().getPlayers().forEach(FeathersNetwork::sendDataMaps);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ModEffects.bindAttributeModifiers();
            ModPotions.registerBrewingRecipes();
        });
    }
}
