package com.darkona.feathers.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * Debug mode only: what the player just spent, and on what, for the debug overlay.
 */
public record SpendDebugPayload(ResourceLocation source, int cost) {

    void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(source);
        buf.writeVarInt(cost);
    }

    static SpendDebugPayload decode(FriendlyByteBuf buf) {
        return new SpendDebugPayload(buf.readResourceLocation(), buf.readVarInt());
    }
}
