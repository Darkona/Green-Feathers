package com.darkona.feathers.compatibility.curios;

import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.item.ModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.UUID;

/**
 * Curios compatibility: the Feather Ring goes in a ring slot. Only loaded when Curios is present.
 */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static void init() {
        CuriosApi.registerCurio(ModItems.FEATHER_RING.get(), new ICurioItem() {
            @Override
            public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid, ItemStack stack) {
                // Curios hands out an id per slot, so two rings halve twice: 1 - 0.5 - 0.5 = weightless.
                return ImmutableMultimap.of(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get(),
                        new AttributeModifier(uuid, "greenfeathers:feather_ring", ModItems.FEATHER_RING_WEIGHT_MULTIPLIER, AttributeModifier.Operation.MULTIPLY_BASE));
            }
        });
    }
}
