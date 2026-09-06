package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.input.SyncedKeyMapping;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import it.unimi.dsi.fastutil.ints.Int2BooleanMap;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;

/** Client key state update. C2S, main-thread handling, required payload. */
public record CPacketKeyDown(Int2BooleanMap updateKeys) implements CustomPacketPayload {

    private static final int MAX_KEYS = 4096;
    public static final Type<CPacketKeyDown> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "key_down"));
    public static final StreamCodec<FriendlyByteBuf, CPacketKeyDown> CODEC = StreamCodec.ofMember(
            CPacketKeyDown::encode, CPacketKeyDown::decode);

    private static CPacketKeyDown decode(FriendlyByteBuf buffer) {
        int size = GTNetwork.readCount(buffer, MAX_KEYS, "key update");
        Int2BooleanMap updates = new Int2BooleanOpenHashMap(size);
        for (int i = 0; i < size; i++) updates.put(buffer.readVarInt(), buffer.readBoolean());
        return new CPacketKeyDown(updates);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (updateKeys.size() > MAX_KEYS) throw new IllegalArgumentException("Too many key updates");
        buffer.writeVarInt(updateKeys.size());
        for (var entry : updateKeys.int2BooleanEntrySet()) {
            buffer.writeVarInt(entry.getIntKey());
            buffer.writeBoolean(entry.getBooleanValue());
        }
    }

    public static void handle(CPacketKeyDown packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            for (var entry : packet.updateKeys().int2BooleanEntrySet()) {
                SyncedKeyMapping keyMapping = SyncedKeyMapping.getFromSyncId(entry.getIntKey());
                if (keyMapping != null) keyMapping.serverActivate(entry.getBooleanValue(), player);
            }
        });
    }

    @Override
    public Type<CPacketKeyDown> type() {
        return TYPE;
    }
}
