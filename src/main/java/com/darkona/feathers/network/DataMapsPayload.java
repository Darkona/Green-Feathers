package com.darkona.feathers.network;

import com.darkona.feathers.api.MountStats;
import com.darkona.feathers.data.DataMaps;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * The armor weight and mount stats data maps as the server loaded them: the client weighs tooltips and decides
 * which creatures are mounts from them too.
 */
public record DataMapsPayload(DataMaps.Raw raw) {

    private static final int MAX_ENTRIES = 1 << 16;

    void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(raw.armor().size());
        for (DataMaps.ArmorEntry entry : raw.armor()) {
            buf.writeUtf(entry.key());
            buf.writeVarInt(entry.weight());
        }
        buf.writeVarInt(raw.mounts().size());
        for (DataMaps.MountEntry entry : raw.mounts()) {
            buf.writeUtf(entry.key());
            buf.writeWithCodec(MountStats.CODEC, entry.stats());
        }
    }

    static DataMapsPayload decode(FriendlyByteBuf buf) {
        int armorCount = count(buf);
        List<DataMaps.ArmorEntry> armor = new ArrayList<>(armorCount);
        for (int i = 0; i < armorCount; i++) armor.add(new DataMaps.ArmorEntry(buf.readUtf(), buf.readVarInt()));
        int mountCount = count(buf);
        List<DataMaps.MountEntry> mounts = new ArrayList<>(mountCount);
        for (int i = 0; i < mountCount; i++) mounts.add(new DataMaps.MountEntry(buf.readUtf(), buf.readWithCodec(MountStats.CODEC)));
        return new DataMapsPayload(new DataMaps.Raw(armor, mounts));
    }

    private static int count(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        if (count < 0 || count > MAX_ENTRIES) throw new DecoderException("Bad data map size " + count);
        return count;
    }
}
