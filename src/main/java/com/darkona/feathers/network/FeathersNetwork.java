package com.darkona.feathers.network;

import com.darkona.feathers.client.ClientFeathersData;
import com.darkona.feathers.core.FeathersData;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class FeathersNetwork {

    private static final String PROTOCOL_VERSION = "5";

    private FeathersNetwork() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(SpendRequestPayload.TYPE, SpendRequestPayload.STREAM_CODEC, SpendRequestPayload::handle);

        // Lambdas, not method references: ClientFeathersData must only load on the client.
        registrar.playToClient(SyncPayload.TYPE, SyncPayload.STREAM_CODEC,
                (payload, context) -> ClientFeathersData.accept(payload));
        registrar.playToClient(SpendDebugPayload.TYPE, SpendDebugPayload.STREAM_CODEC,
                (payload, context) -> ClientFeathersData.acceptDebug(payload));
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

    /**
     * Fake players (machines acting as players) and connections without our channel can't receive payloads.
     */
    private static void send(ServerPlayer player, CustomPacketPayload payload) {
        if (player instanceof FakePlayer || player.connection == null || !player.connection.hasChannel(payload)) return;
        PacketDistributor.sendToPlayer(player, payload);
    }
}
