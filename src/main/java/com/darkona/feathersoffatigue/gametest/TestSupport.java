package com.darkona.feathersoffatigue.gametest;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.config.FeathersCompatConfig;
import com.darkona.feathersoffatigue.core.FeathersData;
import com.darkona.feathersoffatigue.core.FeathersServiceImpl;
import com.darkona.feathersoffatigue.core.FeathersTicker;
import com.darkona.feathersoffatigue.api.registry.FeathersIds;
import com.electronwill.nightconfig.core.file.FileWatcher;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;

/**
 * Creates players for stamina tests. Vanilla's mock server player reports Creative mode, which bypasses stamina.
 * These tests use Forge fake players in Survival mode and tick them directly.
 */
final class TestSupport {

    private TestSupport() {}

    /** Compatibility switches that keep optional thirst or temperature mods from changing ordinary tests. */
    private static final List<ForgeConfigSpec.BooleanValue> COMPATS = List.of(FeathersCompatConfig.COLD_SWEAT, FeathersCompatConfig.THIRST,
            FeathersCompatConfig.DROPLETS_OF_THIRST, FeathersCompatConfig.TAN, FeathersCompatConfig.SEASONS);

    static {
        stopWatchingConfigFiles();
        COMPATS.forEach(value -> value.set(false));
    }

    /**
     * Tests switch config values on and off, and every change rewrites the file. Forge's file watcher would reload
     * each one on its own thread, racing the tests (and sometimes reading a half-written file), so the watch on the
     * mod's server config files does nothing while tests run. Forge removes the watch itself when the server stops.
     */
    private static void stopWatchingConfigFiles() {
        for (ModConfig config : ConfigTracker.INSTANCE.configSets().get(ModConfig.Type.SERVER)) {
            if (!FeathersIds.MOD_ID.equals(config.getModId())) continue;
            try {
                FileWatcher.defaultInstance().setWatch(config.getFullPath(), () -> {});
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    /**
     * Runs {@code test} with every compat switched on.
     */
    static void withCompat(Runnable test) {
        COMPATS.forEach(value -> value.set(true));
        try {
            test.run();
        } finally {
            COMPATS.forEach(value -> value.set(false));
        }
    }

    static ServerPlayer player(GameTestHelper helper) {
        clearNoon(helper.getLevel());
        ServerPlayer player = new TestPlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "feathers-test"));
        player.moveTo(helper.absoluteVec(Vec3.ZERO));
        FeathersAPI.get(player);
        return player;
    }

    /**
     * A 1.19.2 ServerPlayer moves through its connection, which a FakePlayer's ignores, so a FakePlayer stays at the
     * world spawn wherever a test puts it: tests that place the player need the move to be real.
     */
    private static final class TestPlayer extends FakePlayer {

        TestPlayer(ServerLevel level, GameProfile profile) {
            super(level, profile);
        }

        @Override
        public void moveTo(double x, double y, double z) {
            moveTo(x, y, z, getYRot(), getXRot());
        }
    }

    /**
     * Noon under a clear sky, whenever the batch happens to run: the vanilla climate and Serene Seasons read the sun,
     * and rain on a cold biome chills.
     */
    static void clearNoon(ServerLevel level) {
        level.setDayTime(6000);
        level.setWeatherParameters(24000, 0, false, false);
        level.setRainLevel(0);
        level.setThunderLevel(0);
        level.updateSkyBrightness();
    }

    static void tick(ServerPlayer player, int ticks) {
        for (int i = 0; i < ticks; i++) {
            FeathersTicker.tick(player);
            player.tickCount++;
        }
    }

    /**
     * The player's feathers as they would come back from a save.
     */
    static FeathersView saveAndLoad(GameTestHelper helper, ServerPlayer player) {
        CompoundTag tag = FeathersServiceImpl.data(player).serializeNBT();
        FeathersData loaded = new FeathersData();
        loaded.deserializeNBT(tag);
        return loaded;
    }

    /**
     * What later versions' {@code GameTestHelper.assertValueEqual} does.
     */
    static <N> void assertValueEqual(GameTestHelper helper, N actual, N expected, String name) {
        if (!actual.equals(expected)) {
            throw new GameTestAssertException("Expected " + name + " to be " + expected + ", but was " + actual);
        }
    }

    /**
     * What later versions' {@code GameTestHelper.assertTrue} does.
     */
    static void assertTrue(GameTestHelper helper, boolean condition, String message) {
        if (!condition) throw new GameTestAssertException(message);
    }

    /**
     * What later versions' {@code GameTestHelper.assertFalse} does.
     */
    static void assertFalse(GameTestHelper helper, boolean condition, String message) {
        if (condition) throw new GameTestAssertException(message);
    }
}
