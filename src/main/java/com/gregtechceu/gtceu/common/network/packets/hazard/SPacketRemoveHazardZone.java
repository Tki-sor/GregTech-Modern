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

public record SPacketRemoveHazardZone(ChunkPos pos) implements CustomPacketPayload {

    public static final Type<SPacketRemoveHazardZone> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "hazard_remove"));
    public static final StreamCodec<FriendlyByteBuf, SPacketRemoveHazardZone> CODEC = StreamCodec.ofMember(
            SPacketRemoveHazardZone::encode, SPacketRemoveHazardZone::decode);

    private static SPacketRemoveHazardZone decode(FriendlyByteBuf buffer) {
        return new SPacketRemoveHazardZone(buffer.readChunkPos());
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeChunkPos(pos);
    }

    public static void handle(SPacketRemoveHazardZone packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> EnvironmentalHazardClientHandler.INSTANCE
                .removeHazardZone(packet.pos()));
    }

    @Override
    public Type<SPacketRemoveHazardZone> type() {
        return TYPE;
    }
}
