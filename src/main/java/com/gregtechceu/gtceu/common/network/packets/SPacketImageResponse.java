package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.util.ClientImageCache;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;

/** Image response. S2C, main-thread client handling, required payload. */
public record SPacketImageResponse(String url, byte[] imagePart, int index, int totalSize)
        implements CustomPacketPayload {

    public static final int MAX_BYTES_PER_PACKET = 120_000;
    private static final int MAX_URL_BYTES = 2048;
    private static final int MAX_PARTS = 128;
    public static final Type<SPacketImageResponse> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "image_response"));
    public static final StreamCodec<FriendlyByteBuf, SPacketImageResponse> CODEC = StreamCodec.ofMember(
            SPacketImageResponse::encode, SPacketImageResponse::decode);

    private static SPacketImageResponse decode(FriendlyByteBuf buffer) {
        int index = buffer.readVarInt();
        int totalSize = buffer.readVarInt();
        if (index < 0 || totalSize < 1 || totalSize > MAX_PARTS || index >= totalSize) {
            throw new IllegalArgumentException("Invalid image part index");
        }
        String url = GTNetwork.readString(buffer, MAX_URL_BYTES, "image URL");
        return new SPacketImageResponse(url, GTNetwork.readBytes(buffer, MAX_BYTES_PER_PACKET, "image part"), index,
                totalSize);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (index < 0 || totalSize < 1 || totalSize > MAX_PARTS || index >= totalSize ||
                imagePart.length > MAX_BYTES_PER_PACKET) {
            throw new IllegalArgumentException("Invalid image response");
        }
        buffer.writeVarInt(index);
        buffer.writeVarInt(totalSize);
        buffer.writeUtf(url, MAX_URL_BYTES);
        buffer.writeByteArray(imagePart);
    }

    public static void handle(SPacketImageResponse packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            try {
                ClientImageCache.receiveImagePart(packet.url(), packet.imagePart(), packet.index(), packet.totalSize());
            } catch (IOException exception) {
                GTCEu.LOGGER.debug("Failed to receive image response", exception);
            }
        });
    }

    public static void sendImage(String url, byte[] imageBytes, IPayloadContext context) throws IOException {
        if (imageBytes.length == 0) return;
        int packetCount = Math.max(1, GTMath.ceilDiv(imageBytes.length, MAX_BYTES_PER_PACKET));
        if (packetCount > MAX_PARTS) throw new IOException("Image is too large to send");
        for (int i = 0; i < packetCount; i++) {
            int start = i * MAX_BYTES_PER_PACKET;
            byte[] part = ArrayUtils.subarray(imageBytes, start, start + MAX_BYTES_PER_PACKET);
            GTNetwork.reply(context, new SPacketImageResponse(url, part, i, packetCount));
        }
    }

    @Override
    public Type<SPacketImageResponse> type() {
        return TYPE;
    }
}
