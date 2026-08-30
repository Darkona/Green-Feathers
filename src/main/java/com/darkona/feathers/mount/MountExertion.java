package com.darkona.feathers.mount;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.api.SpendResult;
import com.darkona.feathers.api.SpendOptions;
import com.darkona.feathers.api.Stamina;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.core.FeathersTicker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * Mounts' feathers: galloping drains them, jumps cost them (see the horse mixins), rest and time bring
 * them back, and an exhausted mount slows down until it recovers. Works for any living mount a player controls;
 * jumps need a hook per kind of mount. Built on the public API, like the players' basic
 * exertion.
 */
@Mod.EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class MountExertion {

    public static final ResourceLocation GALLOP = id("mount_gallop");
    public static final ResourceLocation JUMP = id("mount_jump");
    private static final ResourceLocation EXHAUSTED_SLOWDOWN = id("mount_exhausted");
    private static final UUID EXHAUSTED_SLOWDOWN_ID = FeathersTicker.modifierId(EXHAUSTED_SLOWDOWN);

    private MountExertion() {}

    @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = false)
    public static void onEntityTick(LivingEvent.LivingUpdateEvent event) {
        LivingEntity mount = event.getEntityLiving();
        if (mount instanceof Player || mount.level.isClientSide() || !mount.isAlive()) return;
        if (FeathersServiceImpl.isMount(mount)) tickMount(mount);
        else if (mount.tickCount % 100 == 0) release(mount);
    }

    /**
     * One tick of a mount's feathers. Public for tests.
     */
    public static void tickMount(LivingEntity mount) {
        Player rider = riderOf(mount);
        FeathersData data = FeathersServiceImpl.data(mount);
        // Unridden mounts with nothing to recover skip the feathers tick: most horses in a world are idle and full.
        // The slowdown still gets checked: a reset or a night's sleep ends exhaustion outside the tick.
        if (rider != null || !data.isAtRest()) {
            if (rider != null && !FeathersServiceImpl.isExempt(rider)) gallop(mount);
            FeathersTicker.tick(mount);
        }
        updateSlowdown(mount, data);
    }

    public static boolean isSlowedDown(LivingEntity mount) {
        AttributeInstance speed = mount.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(EXHAUSTED_SLOWDOWN_ID) != null;
    }

    /**
     * Re-send the mount's feathers to whoever just got on.
     */
    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.isMounting() && event.getEntityBeingMounted() instanceof LivingEntity mount && !mount.level.isClientSide()
                && FeathersServiceImpl.isMount(mount)) {
            FeathersAPI.sync(mount);
        }
    }

    /**
     * The player riding it: its controlling passenger, or its first passenger for mods that steer mounts without
     * making the rider the controller (Mob Wrangler moves them from datapack functions).
     */
    public static Player riderOf(LivingEntity mount) {
        if (mount.getControllingPassenger() instanceof Player player) return player;
        return mount.getFirstPassenger() instanceof Player player ? player : null;
    }

    private static void gallop(LivingEntity mount) {
        MountTuning tuning = FeathersServiceImpl.mountTuning(mount);
        double perSecond = tuning.gallopFeathersPerSecond();
        if (perSecond <= 0) return;
        Vec3 motion = mount.getDeltaMovement();
        double speed = tuning.gallopSpeed();
        if (motion.x * motion.x + motion.z * motion.z < speed * speed) return;
        FeathersAPI.startDrain(mount, GALLOP, Stamina.perTick(perSecond));
    }

    /**
     * A jump of {@code power} out of 100.
     */
    public static boolean chargeJump(LivingEntity mount, int power) {
        Player rider = riderOf(mount);
        return rider == null || chargeJump(mount, rider, power);
    }

    /**
     * The jump's cost, for {@code rider}'s jump. Creative and spectator riders jump for free.
     */
    public static boolean chargeJump(LivingEntity mount, Player rider, int power) {
        if (!FeathersServiceImpl.isMount(mount) || FeathersServiceImpl.isExempt(rider)) return true;
        double feathers = fullJumpFeathers(mount) * Mth.clamp(power, 0, 100) / 100.0;
        return feathers <= 0 || FeathersAPI.spend(mount, JUMP, Stamina.ofFeathers(feathers)).allowed();
    }

    /**
     * Whether the mount has the strength to jump: not exhausted, and a full jump's feathers at hand (feathers or strain
     * room). Asked on both sides: a horse's jump is the rider's client's movement.
     */
    public static boolean canJump(LivingEntity mount) {
        if (!FeathersServiceImpl.isMount(mount)) return true;
        FeathersView view = FeathersAPI.get(mount);
        // A client that hasn't heard the mount's feathers yet (the sync can land before the rider is seated) lets the
        // server decide, instead of grounding the mount.
        if (!view.hasFeathers()) return true;
        return FeathersServiceImpl.simulateAgainst(view, Stamina.ofFeathers(fullJumpFeathers(mount)), true, false) == SpendResult.OK;
    }

    private static double fullJumpFeathers(LivingEntity mount) {
        return FeathersServiceImpl.mountTuning(mount).jumpFeathers();
    }

    private static void updateSlowdown(LivingEntity mount, FeathersData data) {
        // Every tick for every mount: the attribute is only touched when exhaustion flips.
        boolean exhausted = data.exhausted();
        if (exhausted == data.mountSlowed) return;
        data.mountSlowed = exhausted;
        AttributeInstance speed = mount.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null || exhausted == (speed.getModifier(EXHAUSTED_SLOWDOWN_ID) != null)) return;
        if (exhausted) {
            speed.addTransientModifier(new AttributeModifier(EXHAUSTED_SLOWDOWN_ID, EXHAUSTED_SLOWDOWN.toString(),
                    -FeathersServerConfig.MOUNT_EXHAUSTED_SLOWDOWN.get(), AttributeModifier.Operation.MULTIPLY_TOTAL));
        } else {
            speed.removeModifier(EXHAUSTED_SLOWDOWN_ID);
        }
    }

    /**
     * Cleans up a creature that stopped being a mount (config, tags or data map changed), possibly while strained or
     * exhausted: the slowdown, then its feathers' leftovers (see {@link FeathersTicker#releaseExMount}). Public for tests.
     */
    public static void release(LivingEntity entity) {
        // Every creature that isn't a mount comes here: one without feathers never got the slowdown either.
        if (FeathersServiceImpl.dataOrNull(entity) == null) return;
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getModifier(EXHAUSTED_SLOWDOWN_ID) != null) speed.removeModifier(EXHAUSTED_SLOWDOWN_ID);
        FeathersTicker.releaseExMount(entity);
    }
}
