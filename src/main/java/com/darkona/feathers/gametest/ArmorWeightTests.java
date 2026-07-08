package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersEnchantments;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.item.ModItems;
import com.darkona.feathers.weight.ArmorWeights;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertTrue;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
import static com.darkona.feathers.gametest.TestSupport.player;
import static com.darkona.feathers.gametest.TestSupport.tick;

/**
 * Armor weight with the default rules (per material), Lightweight, and the weight multiplier attribute.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class ArmorWeightTests {

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private static void wear(ServerPlayer player, ItemStack... pieces) {
        for (int i = 0; i < pieces.length; i++) player.setItemSlot(SLOTS[i], pieces[i]);
        FeathersAPI.recalculateWeight(player);
    }

    private static ItemStack[] iron() {
        return new ItemStack[]{
                new ItemStack(Items.IRON_HELMET),
                new ItemStack(Items.IRON_CHESTPLATE),
                new ItemStack(Items.IRON_LEGGINGS),
                new ItemStack(Items.IRON_BOOTS)
        };
    }

    /** Runs a test with armor weight enabled, then restores the previous setting. */
    private static void withWeights(GameTestHelper helper, Runnable test) {
        boolean before = FeathersServerConfig.ENABLE_ARMOR_WEIGHTS.get();
        FeathersServerConfig.ENABLE_ARMOR_WEIGHTS.set(true);
        try {
            test.run();
        } finally {
            FeathersServerConfig.ENABLE_ARMOR_WEIGHTS.set(before);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void materialRulesWeighEachPiece(GameTestHelper helper) {
        withWeights(helper, () -> {
            assertValueEqual(helper, FeathersAPI.getPieceWeight(new ItemStack(Items.DIAMOND_CHESTPLATE)), 3.0, "diamond chestplate");
            assertValueEqual(helper, FeathersAPI.getPieceWeight(new ItemStack(Items.LEATHER_BOOTS)), 1.0, "leather boots");
            assertValueEqual(helper, FeathersAPI.getPieceWeight(new ItemStack(Items.CARVED_PUMPKIN)), 0.0, "non-armor on the head");

            ServerPlayer player = player(helper);
            wear(player, iron());
            assertValueEqual(helper, FeathersAPI.get(player).weight(), 8, "full iron");
            assertValueEqual(helper, FeathersAPI.get(player).availableFeathers(), 12, "usable feathers under full iron");
            FeathersData data = FeathersServiceImpl.data(player);
            for (int part = ArmorWeights.HEAD; part <= ArmorWeights.FEET; part++) {
                assertValueEqual(helper, data.weightPart(part), 2, "each iron piece's share of the weight");
            }
            assertValueEqual(helper, data.weightPart(ArmorWeights.OTHER), 0, "nothing else weighs");
        });
    }

    @GameTest(template = "empty")
    public static void disabledWeightsWeighNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        wear(player, iron());
        assertValueEqual(helper, FeathersAPI.get(player).weight(), 0, "weight with armor weights off");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lightweightRemovesAQuarterPerLevel(GameTestHelper helper) {
        withWeights(helper, () -> {
            Enchantment lightweight = FeathersEnchantments.LIGHTWEIGHT.get();

            ItemStack[] netherite = {
                    new ItemStack(Items.NETHERITE_HELMET),
                    new ItemStack(Items.NETHERITE_CHESTPLATE),
                    new ItemStack(Items.NETHERITE_LEGGINGS),
                    new ItemStack(Items.NETHERITE_BOOTS)
            };

            for (ItemStack piece : netherite) piece.enchant(lightweight, 3);

            ServerPlayer player = player(helper);
            wear(player, netherite);
            assertValueEqual(helper, FeathersAPI.getPieceWeight(netherite[1]), 1.0, "netherite chestplate with Lightweight III");
            assertValueEqual(helper, FeathersAPI.get(player).weight(), 4, "full netherite with Lightweight III");

            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.enchant(lightweight, 3);
            assertValueEqual(helper, FeathersAPI.getPieceWeight(boots), 0.25, "leather boots keep a quarter");
        });
    }

    @GameTest(template = "empty")
    public static void weightMultiplierIsNoticedByItself(GameTestHelper helper) {
        withWeights(helper, () -> {
            ServerPlayer player = player(helper);
            wear(player, iron());
            tick(player, 1);

            // What the Feather Ring does, in a Curios slot or the off hand.
            player.getAttribute(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER.get()).addTransientModifier(
                    new AttributeModifier(UUID.nameUUIDFromBytes(id("test_ring").toString().getBytes()), "test_ring", -0.5, AttributeModifier.Operation.MULTIPLY_BASE));
            tick(player, 10);
            assertValueEqual(helper, FeathersAPI.get(player).weight(), 4, "weight with the ring");
            assertValueEqual(helper, FeathersServiceImpl.data(player).weightPart(ArmorWeights.CHEST), 1, "the ring halves each piece's share");
        });
    }

    @GameTest(template = "empty")
    public static void featherRingTooltipNamesItsSlot(GameTestHelper helper) {
        List<Component> tooltip = new ArrayList<>();
        ItemStack ring = new ItemStack(ModItems.FEATHER_RING.get());
        ring.getItem().appendHoverText(ring, null, tooltip, TooltipFlag.Default.NORMAL);
        String where = ModList.get().isLoaded("curios") ? "ring" : "offhand";
        assertTrue(helper, tooltip.size() == 1 && tooltip.get(0).getContents() instanceof TranslatableContents contents
                && contents.getKey().equals("item.greenfeathers.feather_ring.tooltip." + where), "the ring's tooltip names where it goes: " + tooltip);
        helper.succeed();
    }
}
