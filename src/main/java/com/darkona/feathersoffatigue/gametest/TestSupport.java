package com.darkona.feathersoffatigue.gametest;

import com.darkona.feathersoffatigue.api.FeathersAPI;
import com.darkona.feathersoffatigue.api.FeathersView;
import com.darkona.feathersoffatigue.config.FeathersCompatConfig;
import com.darkona.feathersoffatigue.core.FeathersData;
import com.darkona.feathersoffatigue.core.FeathersServiceImpl;
import com.darkona.feathersoffatigue.core.FeathersTicker;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.commands.FillBiomeCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Creates players for stamina tests. Vanilla's mock server player reports Creative mode, which bypasses stamina.
 * These tests use NeoForge fake players in Survival mode and tick them directly.
 */
final class TestSupport {

    private TestSupport() {}

    /** Compatibility switches that keep optional thirst or temperature mods from changing ordinary tests. */
    private static final List<ModConfigSpec.BooleanValue> COMPATS = List.of(FeathersCompatConfig.DROPLETS_OF_THIRST,
            FeathersCompatConfig.SEASONS);

    static {
        COMPATS.forEach(value -> value.set(false));
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
        plains(helper);
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "feathers-test"));
        player.snapTo(helper.absoluteVec(Vec3.ZERO));
        FeathersAPI.get(player);
        return player;
    }

    /**
     * Noon under a clear sky, whenever the batch happens to run: the vanilla climate and Serene Seasons read the sun,
     * and rain on a cold biome chills.
     */
    static void clearNoon(ServerLevel level) {
        level.getServer().setWeatherParameters(24000, 0, false, false);
        level.setRainLevel(0);
        level.setThunderLevel(0);
        setDayTime(level, 6000);
    }

    /**
     * Plains around the test, from below it to the open sky above: the game test world is a desert, whose heat under
     * the sun would double every cost.
     */
    static void plains(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        // Within the command block limit (32768 blocks), up to where a test lifts a player to the open sky.
        FillBiomeCommand.fill(helper.getLevel(), origin.offset(-8, -4, -8), origin.offset(8, 100, 8),
                helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS))
                .ifRight(e -> {
                    // Already plains: a test with several players.
                    if (e.getType() != FillBiomeCommand.ERROR_NO_BIOMES_SET) throw new IllegalStateException("No plains for the test: " + e.getMessage());
                });
    }

    /** Moves the level's clock to {@code ticks} and updates the sky darkness that day and night are read from. */
    static void setDayTime(ServerLevel level, long ticks) {
        level.dimensionType().defaultClock().ifPresent(clock -> level.clockManager().setTotalTicks(clock, ticks));
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
        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
        FeathersServiceImpl.data(player).serialize(output);
        FeathersData loaded = new FeathersData();
        loaded.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
        return loaded;
    }
}
