package com.darkona.feathers.item;

import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.darkona.feathers.api.registry.FeathersIds.id;

public final class ModItems {

    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(FeathersIds.MOD_ID);

    public static final double FEATHER_RING_WEIGHT_MULTIPLIER = -0.5;

    /** With Curios the Feather Ring is worn in a ring slot, without it held in the off hand. */
    public static final boolean CURIOS = ModList.get().isLoaded("curios");

    /**
     * Halves armor weight. Worn in a Curios ring slot when Curios is installed, otherwise held in the off hand. It
     * does nothing special in code: an attribute modifier on armor_weight_multiplier, what any mod's item can do.
     */
    public static final DeferredItem<Item> FEATHER_RING = ITEMS.registerItem("feather_ring", FeatherRingItem::new, featherRingProperties());

    private static Item.Properties featherRingProperties() {
        Item.Properties properties = new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON);
        if (!CURIOS) {
            properties.attributes(ItemAttributeModifiers.builder()
                    .add(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER,
                            new AttributeModifier(id("feather_ring"), FEATHER_RING_WEIGHT_MULTIPLIER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE),
                            EquipmentSlotGroup.OFFHAND)
                    .build());
        }
        return properties;
    }

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
