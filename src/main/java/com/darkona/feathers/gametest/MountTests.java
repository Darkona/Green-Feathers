package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersCommonConfig;
import com.darkona.feathers.mount.MountExertion;
import com.darkona.feathers.mount.MountTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.player;

/**
 * Mounts have feathers: a stamina trait of their own that foals inherit, jumps that cost by power, and an exhausted
 * mount slows down and can't jump.
 */
@GameTestHolder(FeathersIds.MOD_ID)
@PrefixGameTestTemplate(false)
public class MountTests {

    private static Horse horse(GameTestHelper helper) {
        Horse horse = helper.spawn(EntityType.HORSE, new BlockPos(1, 2, 1));
        horse.setTamed(true);
        horse.equipSaddle(new ItemStack(Items.SADDLE), SoundSource.NEUTRAL);
        return horse;
    }

    @GameTest(template = "empty")
    public static void horsesHaveTheirOwnStamina(GameTestHelper helper) {
        Horse horse = horse(helper);
        FeathersView feathers = FeathersAPI.get(horse);
        helper.assertTrue(FeathersAPI.hasFeathers(horse), "a horse has feathers");
        int max = feathers.maxFeathers();
        helper.assertTrue(max >= 14 && max <= 30, "the rolled stamina is within the configured range: " + max);
        helper.assertValueEqual(feathers.stamina(), feathers.maxStamina(), "a new horse is full");

        MountTraits.ensureRolled(horse);
        helper.assertValueEqual(FeathersAPI.get(horse).maxFeathers(), max, "the trait is rolled only once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foalsTakeAfterTheirParents(GameTestHelper helper) {
        Horse mare = horse(helper);
        Horse stallion = horse(helper);
        mare.getAttribute(FeathersAttributes.MAX_FEATHERS).setBaseValue(30);
        stallion.getAttribute(FeathersAttributes.MAX_FEATHERS).setBaseValue(28);
        Horse foal = helper.spawn(EntityType.HORSE, new BlockPos(1, 2, 1));

        NeoForge.EVENT_BUS.post(new BabyEntitySpawnEvent(mare, stallion, foal));
        double trait = foal.getAttribute(FeathersAttributes.MAX_FEATHERS).getBaseValue();
        // Average 29, nudged by at most a sixth of the 14-30 range (about 2.7).
        helper.assertTrue(trait >= 26 && trait <= 30, "the foal takes after strong parents: " + trait);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void jumpsCostByPower(GameTestHelper helper) {
        Horse horse = horse(helper);
        // The rider is passed in: a fake player can't really mount (it isn't in the level).
        int before = FeathersAPI.get(horse).stamina();
        MountExertion.chargeJump(horse, player(helper), 50);
        int expected = Stamina.ofFeathers(FeathersCommonConfig.MOUNT_JUMP_FEATHERS.get() * 0.5);
        helper.assertValueEqual(before - FeathersAPI.get(horse).stamina(), expected, "a half jump costs half a full jump");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void exhaustedHorsesSlowDownAndCantJump(GameTestHelper helper) {
        Horse horse = horse(helper);
        FeathersView feathers = FeathersAPI.get(horse);
        FeathersAPI.spend(horse, id("test"), feathers.maxStamina() + feathers.maxStrain());
        helper.assertTrue(FeathersAPI.get(horse).exhausted(), "spent everything, Strain included");

        MountExertion.tickMount(horse);
        helper.assertTrue(MountExertion.isSlowedDown(horse), "an exhausted horse slows down");
        helper.assertFalse(horse.canJump(), "an exhausted horse can't jump");

        FeathersAPI.reset(horse);
        MountExertion.tickMount(horse);
        helper.assertFalse(MountExertion.isSlowedDown(horse), "a rested horse runs again");
        helper.succeed();
    }

    /**
     * A creature made a mount by data alone: the test datapack (src/gametest/resources) gives pigs mount stats.
     */
    @GameTest(template = "empty")
    public static void dataMakesAnyCreatureAMount(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 1));
        helper.assertTrue(FeathersAPI.hasFeathers(pig), "the data map makes a pig a mount");
        helper.assertValueEqual(FeathersAPI.get(pig).maxFeathers(), 40, "its stamina comes from its stats");
        helper.assertValueEqual(FeathersAPI.getRegenPerSecond(pig), 2.0, "and so does its regeneration");
        helper.assertFalse(FeathersAPI.hasFeathers(helper.spawn(EntityType.COW, new BlockPos(1, 2, 1))), "cows stay out");
        helper.succeed();
    }

    /**
     * The opt-out tag wins even over horses: the test datapack lists donkeys in greenfeathers:no_feathers.
     */
    @GameTest(template = "empty")
    public static void optedOutCreaturesHaveNoFeathers(GameTestHelper helper) {
        helper.assertFalse(FeathersAPI.hasFeathers(helper.spawn(EntityType.DONKEY, new BlockPos(1, 2, 1))), "donkeys opted out");
        helper.assertTrue(FeathersAPI.hasFeathers(helper.spawn(EntityType.MULE, new BlockPos(1, 2, 1))), "mules still tire");
        helper.succeed();
    }

    /**
     * The mod's own data map gives feathers to other mods' mounts when they're installed ({@code -Pcompat=naturalist,
     * mobwrangler}); passes trivially without them.
     */
    @GameTest(template = "empty")
    public static void otherModsMountsHaveFeathers(GameTestHelper helper) {
        checkSpawned(helper, "naturalist", "naturalist:ostrich");
        checkSpawned(helper, "mr_mob_wrangler", "minecraft:sniffer");
        helper.succeed();
    }

    private static void checkSpawned(GameTestHelper helper, String mod, String type) {
        if (!ModList.get().isLoaded(mod)) return;
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(type));
        if (helper.spawn(entityType, new BlockPos(1, 2, 1)) instanceof LivingEntity living) {
            helper.assertTrue(FeathersAPI.hasFeathers(living), type + " has feathers with " + mod);
        }
    }
}
