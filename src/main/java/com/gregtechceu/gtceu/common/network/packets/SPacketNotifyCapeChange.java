package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** Cape update. S2C, main-thread client handling, required payload; cape is explicitly optional. */
public record SPacketNotifyCapeChange(UUID uuid, Identifier cape) implements CustomPacketPayload {

    public static final Type<SPacketNotifyCapeChange> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "cape_change"));
    public static final StreamCodec<FriendlyByteBuf, SPacketNotifyCapeChange> CODEC = StreamCodec.ofMember(
            SPacketNotifyCapeChange::encode, SPacketNotifyCapeChange::decode);

    private static SPacketNotifyCapeChange decode(FriendlyByteBuf buffer) {
        return new SPacketNotifyCapeChange(buffer.readUUID(), buffer.readBoolean() ? buffer.readIdentifier() : null);
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(uuid);
        buffer.writeBoolean(cape != null);
        if (cape != null) buffer.writeIdentifier(cape);
    }

    public static void handle(SPacketNotifyCapeChange packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> CapeRegistry.giveRawCape(packet.uuid(), packet.cape()));
    }

    @Override
    public Type<SPacketNotifyCapeChange> type() {
        return TYPE;
    }
}
