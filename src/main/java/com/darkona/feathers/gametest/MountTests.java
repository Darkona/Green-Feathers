package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.api.registry.FeathersMobEffects;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.mount.MountExertion;
import com.darkona.feathers.mount.MountTraits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.feathers.api.registry.FeathersIds.id;
import static com.darkona.feathers.gametest.TestSupport.assertFalse;
import static com.darkona.feathers.gametest.TestSupport.assertTrue;
import static com.darkona.feathers.gametest.TestSupport.assertValueEqual;
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
        horse.equipSaddle(SoundSource.NEUTRAL);
        return horse;
    }

    @GameTest(template = "empty")
    public static void horsesHaveTheirOwnStamina(GameTestHelper helper) {
        Horse horse = horse(helper);
        FeathersView feathers = FeathersAPI.get(horse);
        assertTrue(helper, FeathersAPI.hasFeathers(horse), "a horse has feathers");
        int max = feathers.maxFeathers();
        assertTrue(helper, max >= 14 && max <= 30, "the rolled stamina is within the configured range: " + max);
        assertValueEqual(helper, feathers.stamina(), feathers.maxStamina(), "a new horse is full");

        MountTraits.ensureRolled(horse);
        assertValueEqual(helper, FeathersAPI.get(horse).maxFeathers(), max, "the trait is rolled only once");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void foalsTakeAfterTheirParents(GameTestHelper helper) {
        Horse mare = horse(helper);
        Horse stallion = horse(helper);
        mare.getAttribute(FeathersAttributes.MAX_FEATHERS.get()).setBaseValue(30);
        stallion.getAttribute(FeathersAttributes.MAX_FEATHERS.get()).setBaseValue(28);
        Horse foal = helper.spawn(EntityType.HORSE, new BlockPos(1, 2, 1));

        MinecraftForge.EVENT_BUS.post(new BabyEntitySpawnEvent(mare, stallion, foal));
        double trait = foal.getAttribute(FeathersAttributes.MAX_FEATHERS.get()).getBaseValue();
        // Average 29, nudged by at most a sixth of the 14-30 range (about 2.7).
        assertTrue(helper, trait >= 26 && trait <= 30, "the foal takes after strong parents: " + trait);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void jumpsCostByPower(GameTestHelper helper) {
        Horse horse = horse(helper);
        // The rider is passed in: a fake player can't really mount (it isn't in the level).
        int before = FeathersAPI.get(horse).stamina();
        MountExertion.chargeJump(horse, player(helper), 50);
        int expected = Stamina.ofFeathers(FeathersServerConfig.MOUNT_JUMP_FEATHERS.get() * 0.5);
        assertValueEqual(helper, before - FeathersAPI.get(horse).stamina(), expected, "a half jump costs half a full jump");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void exhaustedHorsesSlowDownAndCantJump(GameTestHelper helper) {
        Horse horse = horse(helper);
        FeathersView feathers = FeathersAPI.get(horse);
        FeathersAPI.spend(horse, id("test"), feathers.maxStamina() + feathers.maxStrain());
        assertTrue(helper, FeathersAPI.get(horse).exhausted(), "spent everything, Strain included");

        MountExertion.tickMount(horse);
        assertTrue(helper, MountExertion.isSlowedDown(horse), "an exhausted horse slows down");
        assertFalse(helper, horse.canJump(), "an exhausted horse can't jump");

        FeathersAPI.reset(horse);
        MountExertion.tickMount(horse);
        assertFalse(helper, MountExertion.isSlowedDown(horse), "a rested horse runs again");
        helper.succeed();
    }

    /**
     * A creature that stops being a mount (mounts turned off, a reload that drops its tag or data map entry) while
     * strained and exhausted gets back to normal: no Strained effect, no slowdown, no leftover exhaustion. Only the
     * effect this mod put on it goes: one from elsewhere stays.
     */
    @GameTest(template = "empty")
    public static void exMountsAreReleased(GameTestHelper helper) {
        Horse horse = horse(helper);
        FeathersView feathers = FeathersAPI.get(horse);
        FeathersAPI.spend(horse, id("test"), feathers.maxStamina() + feathers.maxStrain());
        MountExertion.tickMount(horse);
        assertTrue(helper, horse.hasEffect(FeathersMobEffects.STRAINED.get()), "a strained horse shows it");
        assertTrue(helper, MountExertion.isSlowedDown(horse), "an exhausted horse slows down");

        MountExertion.release(horse);
        assertFalse(helper, horse.hasEffect(FeathersMobEffects.STRAINED.get()), "the Strained effect goes");
        assertFalse(helper, MountExertion.isSlowedDown(horse), "the slowdown goes");
        feathers = FeathersAPI.get(horse);
        assertValueEqual(helper, feathers.strain(), 0, "no Strain left");
        assertFalse(helper, feathers.exhausted(), "no exhaustion left");

        horse.addEffect(new MobEffectInstance(FeathersMobEffects.STRAINED.get(), 200));
        MountExertion.release(horse);
        assertTrue(helper, horse.hasEffect(FeathersMobEffects.STRAINED.get()), "someone else's Strained effect stays");

        // A mount again: exhaustion slows it down as before.
        horse.removeEffect(FeathersMobEffects.STRAINED.get());
        FeathersAPI.spend(horse, id("test"), FeathersAPI.get(horse).availableStamina() + FeathersAPI.get(horse).maxStrain());
        MountExertion.tickMount(horse);
        assertTrue(helper, MountExertion.isSlowedDown(horse), "an exhausted mount slows down again");
        helper.succeed();
    }

    /**
     * A creature made a mount by data alone: the test datapack (src/gametest/resources) gives pigs mount stats.
     */
    @GameTest(template = "empty")
    public static void dataMakesAnyCreatureAMount(GameTestHelper helper) {
        Pig pig = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 1));
        assertTrue(helper, FeathersAPI.hasFeathers(pig), "the data map makes a pig a mount");
        assertValueEqual(helper, FeathersAPI.get(pig).maxFeathers(), 40, "its stamina comes from its stats");
        assertValueEqual(helper, FeathersAPI.getRegenPerSecond(pig), 2.0, "and so does its regeneration");
        assertFalse(helper, FeathersAPI.hasFeathers(helper.spawn(EntityType.COW, new BlockPos(1, 2, 1))), "cows stay out");
        helper.succeed();
    }

    /**
     * The opt-out tag wins even over horses: the test datapack lists donkeys in greenfeathers:no_feathers.
     */
    @GameTest(template = "empty")
    public static void optedOutCreaturesHaveNoFeathers(GameTestHelper helper) {
        assertFalse(helper, FeathersAPI.hasFeathers(helper.spawn(EntityType.DONKEY, new BlockPos(1, 2, 1))), "donkeys opted out");
        assertTrue(helper, FeathersAPI.hasFeathers(helper.spawn(EntityType.MULE, new BlockPos(1, 2, 1))), "mules still tire");
        helper.succeed();
    }

    /**
     * Other mods' mounts have feathers when they're installed: Naturalist's zebra, a horse of its own
     * ({@code -Pcompat=naturalist}); passes trivially without it.
     */
    @GameTest(template = "empty")
    public static void otherModsMountsHaveFeathers(GameTestHelper helper) {
        checkSpawned(helper, "naturalist", "naturalist:zebra");
        helper.succeed();
    }

    private static void checkSpawned(GameTestHelper helper, String mod, String type) {
        if (!ModList.get().isLoaded(mod)) return;
        EntityType<?> entityType = Registry.ENTITY_TYPE.getOptional(ResourceLocation.tryParse(type)).orElse(null);
        assertTrue(helper, entityType != null, type + " is registered with " + mod);
        if (helper.spawn(entityType, new BlockPos(1, 2, 1)) instanceof LivingEntity living) {
            assertTrue(helper, FeathersAPI.hasFeathers(living), type + " has feathers with " + mod);
        }
    }
}
