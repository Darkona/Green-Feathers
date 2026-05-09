package com.darkona.feathers.network;

import com.darkona.feathers.api.FeathersAPI;
import com.darkona.feathers.api.SpendOptions;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import static com.darkona.feathers.api.registry.FeathersIds.id;

/**
 * A client asks the server to spend its own feathers, for mods that decide actions on the client. It can only
 * ever cost the sender.
 */
public record SpendRequestPayload(ResourceLocation source, int stamina, boolean allowStrain, int regenDelayTicks) implements CustomPacketPayload {

    public static final Type<SpendRequestPayload> TYPE = new Type<>(id("spend_request"));

    public static final StreamCodec<ByteBuf, SpendRequestPayload> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, SpendRequestPayload::source,
            ByteBufCodecs.VAR_INT, SpendRequestPayload::stamina,
            ByteBufCodecs.BOOL, SpendRequestPayload::allowStrain,
            ByteBufCodecs.VAR_INT, SpendRequestPayload::regenDelayTicks,
            SpendRequestPayload::new);

    static void handle(SpendRequestPayload request, IPayloadContext context) {
        if (request.stamina < 0) return;
        // Clamped: a client can't pause its own regeneration for longer than any spend could.
        SpendOptions options = new SpendOptions(false, request.allowStrain, false, Math.max(-1, Math.min(request.regenDelayTicks, 1200)));
        FeathersAPI.spend(context.player(), request.source, request.stamina, options);
        // The client already showed its own prediction: send the server's result back, whatever it charged (a
        // refused spend, a usage multiplier, a stamina modifier), or a sub-feather difference would never sync.
        FeathersAPI.sync(context.player());
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
