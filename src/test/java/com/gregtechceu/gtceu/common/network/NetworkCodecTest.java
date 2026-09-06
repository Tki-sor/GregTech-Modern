package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.common.network.packets.CPacketImageRequest;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeyDown;
import com.gregtechceu.gtceu.common.network.packets.SPacketImageResponse;

import net.minecraft.network.FriendlyByteBuf;

import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2BooleanOpenHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NetworkCodecTest {

    @Test
    void clientImageRequestRoundTrips() {
        CPacketImageRequest original = new CPacketImageRequest("https://example.test/image.png");
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        CPacketImageRequest.CODEC.encode(buffer, original);
        CPacketImageRequest decoded = CPacketImageRequest.CODEC.decode(buffer);
        assertEquals(original, decoded);
    }

    @Test
    void keyUpdateRoundTrips() {
        Int2BooleanOpenHashMap updates = new Int2BooleanOpenHashMap();
        updates.put(3, true);
        updates.put(11, false);
        CPacketKeyDown original = new CPacketKeyDown(updates);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        CPacketKeyDown.CODEC.encode(buffer, original);
        CPacketKeyDown decoded = CPacketKeyDown.CODEC.decode(buffer);
        assertEquals(original.updateKeys(), decoded.updateKeys());
    }

    @Test
    void clientImageResponseRoundTrips() {
        SPacketImageResponse original = new SPacketImageResponse("https://example.test/image.png",
                new byte[] { 1, 2, 3, 5, 8 }, 1, 3);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        SPacketImageResponse.CODEC.encode(buffer, original);
        SPacketImageResponse decoded = SPacketImageResponse.CODEC.decode(buffer);
        assertEquals(original.url(), decoded.url());
        assertArrayEquals(original.imagePart(), decoded.imagePart());
        assertEquals(original.index(), decoded.index());
        assertEquals(original.totalSize(), decoded.totalSize());
    }
}
