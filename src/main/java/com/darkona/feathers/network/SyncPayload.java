package com.darkona.feathers.network;

import com.darkona.feathers.api.RestState;
import com.darkona.feathers.core.FeathersData;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * What the owning client shows: a snapshot of the server's feathers, sent when any of it changes.
 */
public record SyncPayload(int stamina, int maxStamina, int strain, int maxStrain, int bonus, int weight, int regenDelay,
                          boolean exhausted, RestState rest) implements CustomPacketPayload {

    public static final Type<SyncPayload> TYPE = new Type<>(id("sync"));

    private static final StreamCodec<ByteBuf, RestState> REST_CODEC =
            ByteBufCodecs.idMapper(i -> RestState.values()[i], RestState::ordinal);

    public static final StreamCodec<ByteBuf, SyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull SyncPayload decode(@NotNull ByteBuf buf) {
            return new SyncPayload(ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf), ByteBufCodecs.BOOL.decode(buf), REST_CODEC.decode(buf));
        }

        @Override
        public void encode(@NotNull ByteBuf buf, @NotNull SyncPayload p) {
            ByteBufCodecs.VAR_INT.encode(buf, p.stamina);
            ByteBufCodecs.VAR_INT.encode(buf, p.maxStamina);
            ByteBufCodecs.VAR_INT.encode(buf, p.strain);
            ByteBufCodecs.VAR_INT.encode(buf, p.maxStrain);
            ByteBufCodecs.VAR_INT.encode(buf, p.bonus);
            ByteBufCodecs.VAR_INT.encode(buf, p.weight);
            ByteBufCodecs.VAR_INT.encode(buf, p.regenDelay);
            ByteBufCodecs.BOOL.encode(buf, p.exhausted);
            REST_CODEC.encode(buf, p.rest);
        }
    };

    public static SyncPayload of(FeathersData data) {
        return new SyncPayload(data.stamina(), data.maxStamina(), data.strain(), data.maxStrain(), data.bonusStamina(),
                data.weight(), data.regenDelay(), data.exhausted(), data.restState());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
