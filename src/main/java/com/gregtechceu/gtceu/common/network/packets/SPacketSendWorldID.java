package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.capability.WorldIDSaveData;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Server world identity. S2C, main-thread client handling, required payload. */
public record SPacketSendWorldID(String worldId) implements CustomPacketPayload {

    private static final int MAX_WORLD_ID_BYTES = 256;
    public static final Type<SPacketSendWorldID> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "world_id"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSendWorldID> CODEC = StreamCodec.ofMember(
            SPacketSendWorldID::encode, SPacketSendWorldID::decode);

    public SPacketSendWorldID() {
        this(WorldIDSaveData.getWorldID());
    }

    private static SPacketSendWorldID decode(FriendlyByteBuf buffer) {
        return new SPacketSendWorldID(GTNetwork.readString(buffer, MAX_WORLD_ID_BYTES, "world ID"));
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(worldId, MAX_WORLD_ID_BYTES);
    }

    public static void handle(SPacketSendWorldID packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> ClientCacheManager.init(packet.worldId()));
    }

    @Override
    public Type<SPacketSendWorldID> type() {
        return TYPE;
    }
}
