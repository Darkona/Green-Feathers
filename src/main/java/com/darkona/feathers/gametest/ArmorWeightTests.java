package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersEnchantments;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersCommonConfig;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
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
        return new ItemStack[]{new ItemStack(Items.IRON_HELMET), new ItemStack(Items.IRON_CHESTPLATE),
                new ItemStack(Items.IRON_LEGGINGS), new ItemStack(Items.IRON_BOOTS)};
    }

    /** Armor weights are off by default; these tests switch them on while they run. */
    private static void withWeights(GameTestHelper helper, Runnable test) {
        boolean before = FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.get();
        FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.set(true);
        try {
            test.run();
        } finally {
            FeathersCommonConfig.ENABLE_ARMOR_WEIGHTS.set(before);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void materialRulesWeighEachPiece(GameTestHelper helper) {
        withWeights(helper, () -> {
            helper.assertValueEqual(FeathersAPI.getPieceWeight(new ItemStack(Items.DIAMOND_CHESTPLATE)), 3.0, "diamond chestplate");
            helper.assertValueEqual(FeathersAPI.getPieceWeight(new ItemStack(Items.LEATHER_BOOTS)), 1.0, "leather boots");
            helper.assertValueEqual(FeathersAPI.getPieceWeight(new ItemStack(Items.CARVED_PUMPKIN)), 0.0, "non-armor on the head");

            ServerPlayer player = player(helper);
            wear(player, iron());
            helper.assertValueEqual(FeathersAPI.get(player).weight(), 8, "full iron");
            helper.assertValueEqual(FeathersAPI.get(player).availableFeathers(), 12, "usable feathers under full iron");
        });
    }

    @GameTest(template = "empty")
    public static void disabledWeightsWeighNothing(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        wear(player, iron());
        helper.assertValueEqual(FeathersAPI.get(player).weight(), 0, "weight with armor weights off");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void lightweightRemovesAQuarterPerLevel(GameTestHelper helper) {
        withWeights(helper, () -> {
            Holder<Enchantment> lightweight = helper.getLevel().registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(FeathersEnchantments.LIGHTWEIGHT);
            ItemStack[] netherite = {new ItemStack(Items.NETHERITE_HELMET), new ItemStack(Items.NETHERITE_CHESTPLATE),
                    new ItemStack(Items.NETHERITE_LEGGINGS), new ItemStack(Items.NETHERITE_BOOTS)};
            for (ItemStack piece : netherite) piece.enchant(lightweight, 3);

            ServerPlayer player = player(helper);
            wear(player, netherite);
            helper.assertValueEqual(FeathersAPI.getPieceWeight(netherite[1]), 1.0, "netherite chestplate with Lightweight III");
            helper.assertValueEqual(FeathersAPI.get(player).weight(), 4, "full netherite with Lightweight III");

            ItemStack boots = new ItemStack(Items.LEATHER_BOOTS);
            boots.enchant(lightweight, 3);
            helper.assertValueEqual(FeathersAPI.getPieceWeight(boots), 0.25, "leather boots keep a quarter");
        });
    }

    @GameTest(template = "empty")
    public static void weightMultiplierIsNoticedByItself(GameTestHelper helper) {
        withWeights(helper, () -> {
            ServerPlayer player = player(helper);
            wear(player, iron());
            tick(player, 1);

            // What the Feather Ring does, in a Curios slot or the off hand.
            player.getAttribute(FeathersAttributes.ARMOR_WEIGHT_MULTIPLIER).addTransientModifier(
                    new AttributeModifier(id("test_ring"), -0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            tick(player, 10);
            helper.assertValueEqual(FeathersAPI.get(player).weight(), 4, "weight with the ring");
        });
    }
}
