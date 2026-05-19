package com.darkona.feathers.item;

import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.core.FeathersTicker;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.darkona.feathers.api.registry.FeathersIds.id;

public class FeatherRingItem extends Item {

    private static final boolean CURIOS = ModList.get().isLoaded("curios");

    /** Built on first use: the attribute registers after the items. */
    private Multimap<Attribute, AttributeModifier> offhandModifiers;

    public FeatherRingItem(Properties properties) {
        super(properties);
    }

    /**
     * Without Curios the ring works from the off hand; with it, CuriosCompat hands out the modifier per ring slot.
     */
    @Override
    public @NotNull Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(@NotNull EquipmentSlot slot) {
        if (CURIOS || slot != EquipmentSlot.OFFHAND) return super.getDefaultAttributeModifiers(slot);
        if (offhandModifiers == null) {
            offhandModifiers = ImmutableMultimap.of(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get(),
                    new AttributeModifier(FeathersTicker.modifierId(id("feather_ring")), "greenfeathers:feather_ring",
                            ModItems.FEATHER_RING_WEIGHT_MULTIPLIER, AttributeModifier.Operation.MULTIPLY_BASE));
        }
        return offhandModifiers;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.greenfeathers.feather_ring.tooltip." + (CURIOS ? "ring" : "offhand")).withStyle(ChatFormatting.GRAY));
    }
}
