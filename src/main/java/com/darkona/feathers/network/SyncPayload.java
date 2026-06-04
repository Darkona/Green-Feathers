package com.darkona.feathers.network;

import com.darkona.feathers.api.RestState;
import com.darkona.feathers.core.FeathersData;
import com.darkona.feathers.weight.ArmorWeights;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
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
                          boolean exhausted, RestState rest, int[] weightSplit) implements CustomPacketPayload {

    public static final Type<SyncPayload> TYPE = new Type<>(id("sync"));

    private static final RestState[] REST_STATES = RestState.values();
    private static final StreamCodec<ByteBuf, RestState> REST_CODEC =
            ByteBufCodecs.idMapper(i -> REST_STATES[i], RestState::ordinal);

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
            ByteBufCodecs.VAR_INT.encode(buf, p.weightSplit.length);
            for (int value : p.weightSplit) ByteBufCodecs.VAR_INT.encode(buf, value);
        }
    };

    /** The weight split, flattened (see WeightSplit#toArray): the parts, then a pair per colored source. */
    private static int[] readParts(ByteBuf buf) {
        int length = ByteBufCodecs.VAR_INT.decode(buf);
        if (length < ArmorWeights.PARTS || length > ArmorWeights.PARTS + 2 * MAX_SOURCES || (length - ArmorWeights.PARTS) % 2 != 0) {
            throw new DecoderException("Bad weight split length " + length);
        }
        int[] parts = new int[length];
        for (int i = 0; i < length; i++) parts[i] = ByteBufCodecs.VAR_INT.decode(buf);
        return parts;
    }

    private static final int MAX_SOURCES = 64;

    public static SyncPayload of(int entityId, FeathersData data) {
        return new SyncPayload(entityId, data.stamina(), data.maxStamina(), data.strain(), data.maxStrain(), data.bonusStamina(),
                data.weight(), data.regenDelay(), data.exhausted(), data.restState(), weightParts(data));
    }

    /** An empty bar: what a creature that stopped being a mount shows. */
    public static SyncPayload none(int entityId) {
        return new SyncPayload(entityId, 0, 0, 0, 0, 0, 0, 0, false, RestState.NONE, new int[ArmorWeights.PARTS]);
    }

    /** A copy: in singleplayer the payload reaches the client thread without being encoded. */
    private static int[] weightParts(FeathersData data) {
        return data.weightSplit().toArray();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
