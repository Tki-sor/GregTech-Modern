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

public record SPacketAddHazardZone(ChunkPos pos, EnvironmentalHazardSavedData.HazardZone zone)
        implements CustomPacketPayload {

    public static final Type<SPacketAddHazardZone> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "hazard_add"));
    public static final StreamCodec<FriendlyByteBuf, SPacketAddHazardZone> CODEC = StreamCodec.ofMember(
            SPacketAddHazardZone::encode, SPacketAddHazardZone::decode);

    private static SPacketAddHazardZone decode(FriendlyByteBuf buffer) {
        return new SPacketAddHazardZone(buffer.readChunkPos(), EnvironmentalHazardSavedData.HazardZone.fromNetwork(buffer));
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeChunkPos(pos);
        zone.toNetwork(buffer);
    }

    public static void handle(SPacketAddHazardZone packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> EnvironmentalHazardClientHandler.INSTANCE
                .addHazardZone(packet.pos(), packet.zone()));
    }

    @Override
    public Type<SPacketAddHazardZone> type() {
        return TYPE;
    }
}
