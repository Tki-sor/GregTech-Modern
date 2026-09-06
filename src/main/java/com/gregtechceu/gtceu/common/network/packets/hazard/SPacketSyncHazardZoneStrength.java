package com.gregtechceu.gtceu.common.network.packets.hazard;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.EnvironmentalHazardClientHandler;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SPacketSyncHazardZoneStrength(ChunkPos pos, float newAmount) implements CustomPacketPayload {

    public static final Type<SPacketSyncHazardZoneStrength> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "hazard_strength"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncHazardZoneStrength> CODEC = StreamCodec.ofMember(
            SPacketSyncHazardZoneStrength::encode, SPacketSyncHazardZoneStrength::decode);

    private static SPacketSyncHazardZoneStrength decode(FriendlyByteBuf buffer) {
        return new SPacketSyncHazardZoneStrength(buffer.readChunkPos(), buffer.readFloat());
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeChunkPos(pos);
        buffer.writeFloat(newAmount);
    }

    public static void handle(SPacketSyncHazardZoneStrength packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> EnvironmentalHazardClientHandler.INSTANCE
                .updateHazardStrength(packet.pos(), packet.newAmount()));
    }

    @Override
    public Type<SPacketSyncHazardZoneStrength> type() {
        return TYPE;
    }
}
