package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** Starts client-side prospecting cache upload. S2C, main-thread client entry, required payload. */
public record SPacketStartProspectionShare(UUID receiver) implements CustomPacketPayload {

    public static final Type<SPacketStartProspectionShare> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "prospection_start"));
    public static final StreamCodec<FriendlyByteBuf, SPacketStartProspectionShare> CODEC = StreamCodec.ofMember(
            SPacketStartProspectionShare::encode, SPacketStartProspectionShare::decode);

    private static SPacketStartProspectionShare decode(FriendlyByteBuf buffer) {
        return new SPacketStartProspectionShare(buffer.readUUID());
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(receiver);
    }

    public static void handle(SPacketStartProspectionShare packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            if (context.player() == null) return;
            UUID sender = context.player().getUUID();
            Thread.startVirtualThread(() -> {
                boolean first = true;
                for (ClientCacheManager.ProspectionInfo info : ClientCacheManager.getProspectionShareData()) {
                    GTNetwork.sendToServer(new SCPacketShareProspection(sender, packet.receiver(), info.cacheName,
                            info.key, info.isDimCache, info.dim, info.data, first));
                    first = false;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException exception) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            });
        });
    }

    @Override
    public Type<SPacketStartProspectionShare> type() {
        return TYPE;
    }
}
