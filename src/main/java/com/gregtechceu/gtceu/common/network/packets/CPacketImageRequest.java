package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.misc.ImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.IOException;

/** Image request. C2S, main-thread entry, required payload. */
public record CPacketImageRequest(String url) implements CustomPacketPayload {

    private static final int MAX_URL_BYTES = 2048;
    public static final Type<CPacketImageRequest> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "image_request"));
    public static final StreamCodec<FriendlyByteBuf, CPacketImageRequest> CODEC = StreamCodec.ofMember(
            CPacketImageRequest::encode, CPacketImageRequest::decode);

    private static CPacketImageRequest decode(FriendlyByteBuf buffer) {
        return new CPacketImageRequest(GTNetwork.readString(buffer, MAX_URL_BYTES, "image URL"));
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(url, MAX_URL_BYTES);
    }

    public static void handle(CPacketImageRequest packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> ImageCache.queryServerImage(packet.url(), image -> {
            try {
                SPacketImageResponse.sendImage(packet.url(), image, context);
            } catch (IOException exception) {
                GTCEu.LOGGER.debug("Failed to send requested image", exception);
            }
        }));
    }

    @Override
    public Type<CPacketImageRequest> type() {
        return TYPE;
    }
}
