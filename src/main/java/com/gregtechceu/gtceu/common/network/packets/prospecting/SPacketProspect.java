package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.Collection;

/** Bounded common codec for the S2C prospecting payload family. */
public abstract class SPacketProspect<T> implements CustomPacketPayload {

    protected final Table<ResourceKey<Level>, BlockPos, T> data;

    protected SPacketProspect() {
        data = HashBasedTable.create();
    }

    protected SPacketProspect(Collection<ResourceKey<Level>> keys, Collection<BlockPos> positions,
                              Collection<T> prospected) {
        this();
        var keyIterator = keys.iterator();
        var posIterator = positions.iterator();
        var prospectedIterator = prospected.iterator();
        while (keyIterator.hasNext()) data.put(keyIterator.next(), posIterator.next(), prospectedIterator.next());
    }

    protected SPacketProspect(ResourceKey<Level> key, Collection<BlockPos> positions, Collection<T> prospected) {
        this();
        var posIterator = positions.iterator();
        var prospectedIterator = prospected.iterator();
        while (posIterator.hasNext()) data.put(key, posIterator.next(), prospectedIterator.next());
    }

    protected SPacketProspect(ResourceKey<Level> key, BlockPos position, T prospected) {
        this();
        data.put(key, position, prospected);
    }

    protected SPacketProspect(FriendlyByteBuf buffer) {
        this();
        int rowCount = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "prospecting dimension");
        for (int i = 0; i < rowCount; i++) {
            var rowKey = ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer);
            int entryCount = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "prospecting entry");
            for (int j = 0; j < entryCount; j++) {
                data.put(rowKey, BlockPos.STREAM_CODEC.decode(buffer), decodeData(buffer));
            }
        }
    }

    protected abstract void encodeData(FriendlyByteBuf buffer, T value);

    protected abstract T decodeData(FriendlyByteBuf buffer);

    protected final void encode(FriendlyByteBuf buffer) {
        if (data.rowKeySet().size() > GTNetwork.MAX_COLLECTION_ENTRIES) {
            throw new IllegalArgumentException("Too many prospecting dimensions");
        }
        buffer.writeVarInt(data.rowKeySet().size());
        data.rowMap().forEach((key, entries) -> {
            if (entries.size() > GTNetwork.MAX_COLLECTION_ENTRIES) throw new IllegalArgumentException("Too many prospecting entries");
            ResourceKey.streamCodec(Registries.DIMENSION).encode(buffer, key);
            buffer.writeVarInt(entries.size());
            entries.forEach((pos, value) -> {
                BlockPos.STREAM_CODEC.encode(buffer, pos);
                encodeData(buffer, value);
            });
        });
    }
}
