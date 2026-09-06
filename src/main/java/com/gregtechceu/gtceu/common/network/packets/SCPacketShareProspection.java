package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.ClientCacheManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.UUID;

/**
 * Prospecting cache transfer. It is genuinely bidirectional: C2S uploads one cache entry and S2C
 * forwards it to the target client. The packet is required and is handled on the main thread.
 */
public record SCPacketShareProspection(UUID sender, UUID receiver, String cacheName, String key,
                                       boolean isDimCache, ResourceKey<Level> dimension, CompoundTag data,
                                       boolean first) implements CustomPacketPayload {

    private static final int MAX_NAME_BYTES = 256;
    public static final Type<SCPacketShareProspection> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "prospection_share"));
    public static final StreamCodec<FriendlyByteBuf, SCPacketShareProspection> CODEC = StreamCodec.ofMember(
            SCPacketShareProspection::encode, SCPacketShareProspection::decode);

    private static SCPacketShareProspection decode(FriendlyByteBuf buffer) {
        UUID sender = buffer.readUUID();
        UUID receiver = buffer.readUUID();
        String cacheName = GTNetwork.readString(buffer, MAX_NAME_BYTES, "cache name");
        String key = GTNetwork.readString(buffer, MAX_NAME_BYTES, "cache key");
        boolean dimensionCache = buffer.readBoolean();
        ResourceKey<Level> dimension = ResourceKey.streamCodec(Registries.DIMENSION).decode(buffer);
        CompoundTag data = buffer.readNbt();
        if (data == null) throw new IllegalArgumentException("Missing prospecting cache data");
        return new SCPacketShareProspection(sender, receiver, cacheName, key, dimensionCache, dimension, data,
                buffer.readBoolean());
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeUUID(sender);
        buffer.writeUUID(receiver);
        buffer.writeUtf(cacheName, MAX_NAME_BYTES);
        buffer.writeUtf(key, MAX_NAME_BYTES);
        buffer.writeBoolean(isDimCache);
        ResourceKey.streamCodec(Registries.DIMENSION).encode(buffer, dimension);
        buffer.writeNbt(data);
        buffer.writeBoolean(first);
    }

    public static void handle(SCPacketShareProspection packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            if (context.flow() == PacketFlow.CLIENTBOUND) {
                PlayerInfo senderInfo = Objects.requireNonNull(Minecraft.getInstance().getConnection())
                        .getPlayerInfo(packet.sender());
                if (packet.first() && senderInfo != null && Minecraft.getInstance().player != null) {
                    var playerName = senderInfo.getTabListDisplayName() != null ? senderInfo.getTabListDisplayName() :
                            net.minecraft.network.chat.Component.literal(senderInfo.getProfile().getName());
                    Minecraft.getInstance().player.sendSystemMessage(net.minecraft.network.chat.Component
                            .translatable("command.gtceu.share_prospection_data.notification", playerName));
                }
                ClientCacheManager.processProspectionShare(packet.cacheName(), packet.key(), packet.isDimCache(),
                        packet.dimension(), packet.data());
                return;
            }

            if (context.player() instanceof net.minecraft.server.level.ServerPlayer) {
                var target = GTCEu.getMinecraftServer().getPlayerList().getPlayer(packet.receiver());
                GTNetwork.sendToPlayer(target, packet);
            }
        });
    }

    @Override
    public Type<SCPacketShareProspection> type() {
        return TYPE;
    }
}
