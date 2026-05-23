package com.darkona.feathers.network;

import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.data.DataMaps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import static com.darkona.feathers.api.registry.FeathersIds.id;

public final class FeathersNetwork {

    private static final String PROTOCOL_VERSION = "5";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(id("main"), () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    private FeathersNetwork() {}

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(SpendRequestPayload.class, id++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SpendRequestPayload::encode).decoder(SpendRequestPayload::decode)
                .consumerMainThread(SpendRequestPayload::handle).add();

        // Lambdas run through DistExecutor: ClientFeathersData and DataMaps' client side must only load on the client.
        CHANNEL.messageBuilder(SyncPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncPayload::encode).decoder(SyncPayload::decode)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFeathersData.accept(payload)))
                .add();
        CHANNEL.messageBuilder(SpendDebugPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SpendDebugPayload::encode).decoder(SpendDebugPayload::decode)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFeathersData.acceptDebug(payload)))
                .add();
        CHANNEL.messageBuilder(DataMapsPayload.class, id++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DataMapsPayload::encode).decoder(DataMapsPayload::decode)
                .consumerMainThread((payload, context) -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFeathersData.acceptDataMaps(payload)))
                .add();
    }

    /**
     * Sends {@code entity}'s feathers (the player's own, or its mount's) to {@code player}.
     */
    public static void sendSync(ServerPlayer player, LivingEntity entity, FeathersData data) {
        send(player, SyncPayload.of(entity.getId(), data));
    }

    public static void sendSpendDebug(LivingEntity entity, ResourceLocation source, int cost) {
        if (entity instanceof ServerPlayer player) send(player, new SpendDebugPayload(source, cost));
    }

    /** The data maps, on joining and after every datapack reload. */
    public static void sendDataMaps(ServerPlayer player) {
        send(player, new DataMapsPayload(DataMaps.raw()));
    }

    /**
     * Fake players (machines acting as players) and connections without our channel can't receive payloads.
     */
    private static void send(ServerPlayer player, Object payload) {
        if (player instanceof FakePlayer || player.connection == null || !CHANNEL.isRemotePresent(player.connection.connection)) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
    }
}
