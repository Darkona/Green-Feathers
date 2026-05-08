package com.darkona.feathers.network;

import com.darkona.feathers.api.RestState;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.weight.ArmorWeights;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * What the owning client shows: a snapshot of the server's feathers for the player itself or the mount it rides,
 * sent when any of it changes.
 */
public record SyncPayload(int entityId, int stamina, int maxStamina, int strain, int maxStrain, int bonus, int weight, int regenDelay,
                          boolean exhausted, RestState rest, int[] weightParts) implements CustomPacketPayload {

    public static final Type<SyncPayload> TYPE = new Type<>(id("sync"));

    private static final StreamCodec<ByteBuf, RestState> REST_CODEC =
            ByteBufCodecs.idMapper(i -> RestState.values()[i], RestState::ordinal);

    public static final StreamCodec<ByteBuf, SyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SyncPayload decode(@NotNull ByteBuf buf) {
            return new SyncPayload(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.BOOL.decode(buf), REST_CODEC.decode(buf), readParts(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull SyncPayload p) {
            ByteBufCodecs.VAR_INT.encode(buf, p.entityId);
            ByteBufCodecs.VAR_INT.encode(buf, p.stamina);
            ByteBufCodecs.VAR_INT.encode(buf, p.maxStamina);
            ByteBufCodecs.VAR_INT.encode(buf, p.strain);
            ByteBufCodecs.VAR_INT.encode(buf, p.maxStrain);
            ByteBufCodecs.VAR_INT.encode(buf, p.bonus);
            ByteBufCodecs.VAR_INT.encode(buf, p.weight);
            ByteBufCodecs.VAR_INT.encode(buf, p.regenDelay);
            ByteBufCodecs.BOOL.encode(buf, p.exhausted);
            REST_CODEC.encode(buf, p.rest);
            for (int part : p.weightParts) ByteBufCodecs.VAR_INT.encode(buf, part);
        }
    };

    private static int[] readParts(ByteBuf buf) {
        int[] parts = new int[ArmorWeights.PARTS];
        for (int i = 0; i < parts.length; i++) parts[i] = ByteBufCodecs.VAR_INT.decode(buf);
        return parts;
    }

    public static SyncPayload of(int entityId, FeathersData data) {
        return new SyncPayload(entityId, data.stamina(), data.maxStamina(), data.strain(), data.maxStrain(), data.bonusStamina(),
                data.weight(), data.regenDelay(), data.exhausted(), data.restState(), weightParts(data));
    }

    /** A copy: in singleplayer the payload reaches the client thread without being encoded. */
    private static int[] weightParts(FeathersData data) {
        int[] parts = new int[ArmorWeights.PARTS];
        for (int i = 0; i < parts.length; i++) parts[i] = data.weightPart(i);
        return parts;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
