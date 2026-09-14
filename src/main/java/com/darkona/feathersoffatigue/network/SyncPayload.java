package com.darkona.feathersoffatigue.network;

import com.darkona.feathersoffatigue.api.RestState;
import com.darkona.feathersoffatigue.core.FeathersData;
import com.darkona.feathersoffatigue.weight.ArmorWeights;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

/**
 * What the owning client shows: a snapshot of the server's feathers for the player itself or the mount it rides,
 * sent when any of it changes.
 */
public record SyncPayload(int entityId, int stamina, int maxStamina, int strain, int maxStrain, int bonus, int weight, int regenDelay,
                          boolean exhausted, RestState rest, int[] weightSplit) {

    private static final RestState[] REST_STATES = RestState.values();
    private static final int MAX_SOURCES = 64;

    void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeVarInt(stamina);
        buf.writeVarInt(maxStamina);
        buf.writeVarInt(strain);
        buf.writeVarInt(maxStrain);
        buf.writeVarInt(bonus);
        buf.writeVarInt(weight);
        buf.writeVarInt(regenDelay);
        buf.writeBoolean(exhausted);
        buf.writeVarInt(rest.ordinal());
        buf.writeVarInt(weightSplit.length);
        for (int value : weightSplit) buf.writeVarInt(value);
    }

    static SyncPayload decode(FriendlyByteBuf buf) {
        int entityId = buf.readVarInt(), stamina = buf.readVarInt(), maxStamina = buf.readVarInt(), strain = buf.readVarInt(),
                maxStrain = buf.readVarInt(), bonus = buf.readVarInt(), weight = buf.readVarInt(), regenDelay = buf.readVarInt();
        boolean exhausted = buf.readBoolean();
        int rest = buf.readVarInt();
        if (rest < 0 || rest >= REST_STATES.length) throw new DecoderException("Bad rest state " + rest);
        return new SyncPayload(entityId, stamina, maxStamina, strain, maxStrain, bonus, weight, regenDelay, exhausted, REST_STATES[rest], readParts(buf));
    }

    /** The weight split, flattened (see WeightSplit#toArray): the parts, then a pair per colored source. */
    private static int[] readParts(FriendlyByteBuf buf) {
        int length = buf.readVarInt();
        if (length < ArmorWeights.PARTS || length > ArmorWeights.PARTS + 2 * MAX_SOURCES || (length - ArmorWeights.PARTS) % 2 != 0) {
            throw new DecoderException("Bad weight split length " + length);
        }
        int[] parts = new int[length];
        for (int i = 0; i < length; i++) parts[i] = buf.readVarInt();
        return parts;
    }

    /** An empty bar: what a creature that stopped being a mount shows. */
    public static SyncPayload none(int entityId) {
        return new SyncPayload(entityId, 0, 0, 0, 0, 0, 0, 0, false, RestState.NONE, new int[ArmorWeights.PARTS]);
    }

    public static SyncPayload of(int entityId, FeathersData data) {
        return new SyncPayload(entityId, data.stamina(), data.maxStamina(), data.strain(), data.maxStrain(), data.bonusStamina(),
                data.weight(), data.regenDelay(), data.exhausted(), data.restState(), data.weightSplit().toArray());
    }
}
