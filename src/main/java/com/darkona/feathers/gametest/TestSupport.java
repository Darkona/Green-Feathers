package com.darkona.feathers.gametest;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.FeathersView;
import com.darkona.feathers.config.FeathersCompatConfig;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.core.FeathersServiceImpl;
import com.darkona.feathers.core.FeathersTicker;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import java.util.List;
import java.util.UUID;

/**
 * Players for feathers tests. Vanilla's mock server player reports itself as creative, which feathers exempt by
 * design, so these are NeoForge fake players in survival. They aren't ticked by the server: tests tick them.
 */
final class TestSupport {

    private TestSupport() {}

    /** Compat switches, off while ordinary tests run so a thirst or temperature mod on the runtime can't skew them. */
    private static final List<ModConfigSpec.BooleanValue> COMPATS = List.of(FeathersCompatConfig.COLD_SWEAT, FeathersCompatConfig.THIRST,
            FeathersCompatConfig.TAN, FeathersCompatConfig.LSO, FeathersCompatConfig.SEASONS);

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
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "feathers-test"));
        player.moveTo(helper.absoluteVec(Vec3.ZERO));
        FeathersAPI.get(player);
        return player;
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
        CompoundTag tag = FeathersServiceImpl.data(player).serializeNBT(helper.getLevel().registryAccess());
        FeathersData loaded = new FeathersData();
        loaded.deserializeNBT(helper.getLevel().registryAccess(), tag);
        return loaded;
    }
}
