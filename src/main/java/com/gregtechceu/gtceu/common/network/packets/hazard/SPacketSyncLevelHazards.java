package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.capability.EnvironmentalHazardSavedData;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record SPacketSyncLevelHazards(Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> map)
        implements CustomPacketPayload {

    public static final Type<SPacketSyncLevelHazards> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "hazard_level_sync"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncLevelHazards> CODEC = StreamCodec.ofMember(
            SPacketSyncLevelHazards::encode, SPacketSyncLevelHazards::decode);

    private static SPacketSyncLevelHazards decode(FriendlyByteBuf buffer) {
        int size = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "hazard zone");
        Map<ChunkPos, EnvironmentalHazardSavedData.HazardZone> map = new LinkedHashMap<>(size);
        for (int i = 0; i < size; i++) {
            map.put(buffer.readChunkPos(), EnvironmentalHazardSavedData.HazardZone.fromNetwork(buffer));
        }
        return new SPacketSyncLevelHazards(map);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (map.size() > GTNetwork.MAX_COLLECTION_ENTRIES) throw new IllegalArgumentException("Too many hazard zones");
        buffer.writeVarInt(map.size());
        map.forEach((pos, zone) -> {
            buffer.writeChunkPos(pos);
            zone.toNetwork(buffer);
        });
    }

    public static void handle(SPacketSyncLevelHazards packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> EnvironmentalHazardClientHandler.INSTANCE
                .updateHazardMap(packet.map()));
    }

    @Override
    public Type<SPacketSyncLevelHazards> type() {
        return TYPE;
    }
}
