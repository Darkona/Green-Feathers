package com.darkona.feathers.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FeatherRingItem extends Item {

    public FeatherRingItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        String where = ModList.get().isLoaded("curios") ? "ring" : "offhand";
        tooltip.add(Component.translatable("item.greenfeathers.feather_ring.tooltip." + where).withStyle(ChatFormatting.GRAY));
    }
}
