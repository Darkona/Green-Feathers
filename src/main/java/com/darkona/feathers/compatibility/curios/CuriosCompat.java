package com.darkona.feathers.compatibility.curios;

import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.item.FeatherRingItem;
import com.darkona.feathers.item.ModItems;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.capability.ICurio;

import java.util.UUID;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Curios compatibility: the Feather Ring goes in a ring slot. Only loaded when Curios is present. Curios 5.1 (1.19.2)
 * takes its slot types by IMC and its curios as an item capability.
 */
public final class CuriosCompat {

    private static final ResourceLocation CURIO = id("feather_ring_curio");

    private CuriosCompat() {}

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener((InterModEnqueueEvent event) ->
                InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.RING.getMessageBuilder().build()));
        MinecraftForge.EVENT_BUS.addGenericListener(ItemStack.class, CuriosCompat::attachCurio);
    }

    private static void attachCurio(AttachCapabilitiesEvent<ItemStack> event) {
        ItemStack stack = event.getObject();
        if (stack.getItem() instanceof FeatherRingItem) event.addCapability(CURIO, new RingProvider(stack));
    }

    private static final class RingProvider implements ICapabilityProvider {

        private final LazyOptional<ICurio> curio;

        RingProvider(ItemStack stack) {
            curio = LazyOptional.of(() -> new Ring(stack));
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return CuriosCapability.ITEM.orEmpty(cap, curio);
        }
    }

    private record Ring(ItemStack stack) implements ICurio {

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public Multimap<Attribute, AttributeModifier> getAttributeModifiers(SlotContext slotContext, UUID uuid) {
            // Curios hands out an id per slot, so two rings halve twice: 1 - 0.5 - 0.5 = weightless.
            return ImmutableMultimap.of(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get(),
                    new AttributeModifier(uuid, "greenfeathers:feather_ring", ModItems.FEATHER_RING_WEIGHT_MULTIPLIER, AttributeModifier.Operation.MULTIPLY_BASE));
        }
    }
}
