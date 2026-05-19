package com.darkona.feathers.item;

import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, FeathersIds.MOD_ID);

    public static final double FEATHER_RING_WEIGHT_MULTIPLIER = -0.5;

    /**
     * Halves armor weight. Worn in a Curios ring slot when Curios is installed, otherwise held in the off hand. It
     * does nothing special in code: an attribute modifier on armor_weight_multiplier, what any mod's item can do.
     */
    public static final RegistryObject<Item> FEATHER_RING = ITEMS.register("feather_ring",
            () -> new FeatherRingItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(FEATHER_RING);
        }
    }

    private ModItems() {}
}
