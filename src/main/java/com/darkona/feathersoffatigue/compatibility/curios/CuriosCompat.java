package com.darkona.feathersoffatigue.compatibility.curios;

import com.darkona.feathersoffatigue.api.registry.FeathersAttributes;
import com.darkona.feathersoffatigue.item.ModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Curios compatibility: the Feather Ring goes in a ring slot. Only loaded when Curios is present.
 */
public final class CuriosCompat {

    private CuriosCompat() {}

    public static void init() {
        CuriosApi.registerCurio(ModItems.FEATHER_RING.get(), new ICurioItem() {
            @Override
            public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, Identifier id, ItemStack stack) {
                // Curios hands out an id per slot, so two rings halve twice: 1 - 0.5 - 0.5 = weightless.
                return ImmutableMultimap.of(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER,
                        new AttributeModifier(id, ModItems.FEATHER_RING_WEIGHT_MULTIPLIER, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        });
    }
}
