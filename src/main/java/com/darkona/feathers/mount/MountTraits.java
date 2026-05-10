package com.darkona.feathers.mount;

import com.darkona.feathers.api.MountStats;
import com.darkona.feathers.api.registry.FeathersAttributes;
import com.darkona.feathers.api.registry.FeathersIds;
import com.darkona.feathers.config.FeathersServerConfig;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.core.FeathersServiceImpl;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.BabyEntitySpawnEvent;

/**
 * A mount's stamina is a hidden trait, like its speed or health: rolled once when it first appears, saved as the base
 * of its max feathers attribute, and passed on to foals as their parents' average with some variation.
 */
@EventBusSubscriber(modid = FeathersIds.MOD_ID)
public final class MountTraits {

    /** Marks the trait as rolled, in the mount's saved feathers counters. */
    private static final String ROLLED = "mount.stamina_rolled";

    private MountTraits() {}

    /**
     * Rolls the mount's max feathers if it never had them. Near the middle of the configured range more often than
     * at its ends: the average of three rolls.
     */
    public static void ensureRolled(LivingEntity mount) {
        FeathersData data = FeathersServiceImpl.data(mount);
        if (data.getCounter(ROLLED) != 0) return;
        RandomSource random = mount.getRandom();
        double roll = (random.nextDouble() + random.nextDouble() + random.nextDouble()) / 3.0;
        setTrait(mount, data, Mth.lerp(roll, min(mount), max(mount)));
    }

    /**
     * Foals get their parents' average, nudged by up to a sixth of the range either way.
     */
    @SubscribeEvent
    public static void onBreed(BabyEntitySpawnEvent event) {
        if (!(event.getChild() instanceof LivingEntity foal) || foal.level().isClientSide() || !FeathersServiceImpl.isMount(foal)
                || !(event.getParentA() instanceof LivingEntity a) || !(event.getParentB() instanceof LivingEntity b)) return;

        double average = (trait(a, foal) + trait(b, foal)) / 2.0;
        double spread = (max(foal) - min(foal)) / 6.0;
        double feathers = average + (foal.getRandom().nextDouble() * 2.0 - 1.0) * spread;
        setTrait(foal, FeathersServiceImpl.data(foal), Mth.clamp(feathers, min(foal), max(foal)));
    }

    private static double trait(LivingEntity parent, LivingEntity foal) {
        AttributeInstance attr = parent.getAttribute(FeathersAttributes.MAX_FEATHERS);
        return attr != null ? attr.getBaseValue() : (min(foal) + max(foal)) / 2.0;
    }

    private static void setTrait(LivingEntity mount, FeathersData data, double feathers) {
        AttributeInstance attr = mount.getAttribute(FeathersAttributes.MAX_FEATHERS);
        if (attr == null) return;
        attr.setBaseValue(Math.round(feathers));
        data.setCounter(ROLLED, 1);
    }

    /** The creature's range: its mount stats data map entry, else the config. */
    private static double min(LivingEntity mount) {
        MountStats stats = FeathersServiceImpl.mountStats(mount);
        return Math.min(stats.minFeathers().orElseGet(FeathersServerConfig.MOUNT_MIN_FEATHERS), stats.maxFeathers().orElseGet(FeathersServerConfig.MOUNT_MAX_FEATHERS));
    }

    private static double max(LivingEntity mount) {
        MountStats stats = FeathersServiceImpl.mountStats(mount);
        return Math.max(stats.minFeathers().orElseGet(FeathersServerConfig.MOUNT_MIN_FEATHERS), stats.maxFeathers().orElseGet(FeathersServerConfig.MOUNT_MAX_FEATHERS));
    }
}
