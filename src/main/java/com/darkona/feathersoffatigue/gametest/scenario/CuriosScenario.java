package com.darkona.feathersoffatigue.gametest.scenario;

import com.darkona.feathersoffatigue.api.registry.FeathersAttributes;
import com.darkona.feathersoffatigue.item.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;

import java.util.UUID;

/**
 * Reads the Feather Ring as Curios sees it. Only loaded when Curios is present.
 */
public final class CuriosScenario {

    private CuriosScenario() {}

    public static boolean hasRingSlot() {
        return CuriosApi.getSlotHelper().getSlotType("ring").isPresent();
    }

    /** The armor weight multiplier modifier a Feather Ring gives in the first ring slot; NaN if it isn't a curio. */
    public static double ringWeightMultiplier(LivingEntity wearer) {
        ItemStack ring = new ItemStack(ModItems.FEATHER_RING.get());
        return ring.getCapability(CuriosCapability.ITEM)
                .map(curio -> curio.getAttributeModifiers(new SlotContext("ring", wearer, 0, false, true), UUID.randomUUID())
                        .get(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get()).stream().mapToDouble(AttributeModifier::getAmount).sum())
                .orElse(Double.NaN);
    }
}
