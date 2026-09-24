package com.darkona.feathersoffatigue.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class FeatherRingItem extends Item {

    /** Where the ring goes: a Curios ring slot, or the off hand. Built once, since a tooltip is rebuilt every frame. */
    private static final String TOOLTIP = "item.feathers_of_fatigue.feather_ring.tooltip." + (ModItems.CURIOS ? "ring" : "offhand");

    public FeatherRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay display,
            @NotNull Consumer<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.accept(Component.translatable(TOOLTIP).withStyle(ChatFormatting.GRAY));
    }
}
