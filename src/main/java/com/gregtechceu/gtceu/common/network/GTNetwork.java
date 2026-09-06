package com.gregtechceu.gtceu.common.network;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.network.packets.CPacketImageRequest;
import com.gregtechceu.gtceu.common.network.packets.CPacketKeyDown;
import com.gregtechceu.gtceu.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.gregtechceu.gtceu.common.network.packets.SCPacketShareProspection;
import com.gregtechceu.gtceu.common.network.packets.SPacketImageResponse;
import com.gregtechceu.gtceu.common.network.packets.SPacketNotifyCapeChange;
import com.gregtechceu.gtceu.common.network.packets.SPacketSendWorldID;
import com.gregtechceu.gtceu.common.network.packets.SPacketStartProspectionShare;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncBedrockOreVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncFluidVeins;
import com.gregtechceu.gtceu.common.network.packets.SPacketSyncOreVeins;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketAddHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketRemoveHazardZone;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncHazardZoneStrength;
import com.gregtechceu.gtceu.common.network.packets.hazard.SPacketSyncLevelHazards;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockFluid;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectBedrockOre;
import com.gregtechceu.gtceu.common.network.packets.prospecting.SPacketProspectOre;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * GTM's play payload boundary.
 *
 * <p>All payloads are required members of the versioned GTM play protocol. A connection with an
 * older or newer GTM network version is rejected during NeoForge negotiation. Serverbound
 * payloads are limited by NeoForge's 32 KiB play limit and clientbound payloads by its 1 MiB
 * play limit; individual codecs below apply stricter family-specific bounds where appropriate.
 * Decode failures are protocol failures and are handled by NeoForge's payload guard. Handler
 * callbacks are explicitly scheduled on the main thread and catch application failures so a bad
 * payload cannot take down the logical server or client.</p>
 */
public final class GTNetwork {

    public static final String PROTOCOL_VERSION = "2";
    public static final int MAX_SERVERBOUND_BYTES = 32 * 1024;
    public static final int MAX_CLIENTBOUND_BYTES = 1024 * 1024;
    public static final int MAX_STRING_BYTES = 32 * 1024;
    public static final int MAX_COLLECTION_ENTRIES = 65_536;

    private GTNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION).executesOn(HandlerThread.MAIN);

        registrar.playToServer(CPacketImageRequest.TYPE, CPacketImageRequest.CODEC,
                CPacketImageRequest::handle);
        registrar.playToServer(CPacketKeyDown.TYPE, CPacketKeyDown.CODEC, CPacketKeyDown::handle);

        registrar.playBidirectional(SCPacketMonitorGroupNBTChange.TYPE, SCPacketMonitorGroupNBTChange.CODEC,
                SCPacketMonitorGroupNBTChange::handle);
        registrar.playToClient(SPacketImageResponse.TYPE, SPacketImageResponse.CODEC);
        registrar.playToClient(SPacketNotifyCapeChange.TYPE, SPacketNotifyCapeChange.CODEC);
        registrar.playToClient(SPacketSendWorldID.TYPE, SPacketSendWorldID.CODEC);
        registrar.playToClient(SPacketStartProspectionShare.TYPE, SPacketStartProspectionShare.CODEC);
        registrar.playToClient(SPacketSyncOreVeins.TYPE, SPacketSyncOreVeins.CODEC);
        registrar.playToClient(SPacketSyncFluidVeins.TYPE, SPacketSyncFluidVeins.CODEC);
        registrar.playToClient(SPacketSyncBedrockOreVeins.TYPE, SPacketSyncBedrockOreVeins.CODEC);
        registrar.playToClient(SPacketAddHazardZone.TYPE, SPacketAddHazardZone.CODEC);
        registrar.playToClient(SPacketRemoveHazardZone.TYPE, SPacketRemoveHazardZone.CODEC);
        registrar.playToClient(SPacketSyncHazardZoneStrength.TYPE, SPacketSyncHazardZoneStrength.CODEC);
        registrar.playToClient(SPacketSyncLevelHazards.TYPE, SPacketSyncLevelHazards.CODEC);
        registrar.playToClient(SPacketProspectOre.TYPE, SPacketProspectOre.CODEC);
        registrar.playToClient(SPacketProspectBedrockOre.TYPE, SPacketProspectBedrockOre.CODEC);
        registrar.playToClient(SPacketProspectBedrockFluid.TYPE, SPacketProspectBedrockFluid.CODEC);

        // This payload is intentionally bidirectional: the client uploads a cache entry and the
        // server forwards it to the selected player. The clientbound handler is registered below.
        registrar.playBidirectional(SCPacketShareProspection.TYPE, SCPacketShareProspection.CODEC,
                SCPacketShareProspection::handle);
    }

    /** Registers clientbound handlers through the 26.1 client lifecycle. */
    public static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(SCPacketMonitorGroupNBTChange.TYPE, SCPacketMonitorGroupNBTChange::handle);
        event.register(SCPacketShareProspection.TYPE, SCPacketShareProspection::handle);
        event.register(SPacketImageResponse.TYPE, SPacketImageResponse::handle);
        event.register(SPacketNotifyCapeChange.TYPE, SPacketNotifyCapeChange::handle);
        event.register(SPacketSendWorldID.TYPE, SPacketSendWorldID::handle);
        event.register(SPacketStartProspectionShare.TYPE, SPacketStartProspectionShare::handle);
        event.register(SPacketSyncOreVeins.TYPE, SPacketSyncOreVeins::handle);
        event.register(SPacketSyncFluidVeins.TYPE, SPacketSyncFluidVeins::handle);
        event.register(SPacketSyncBedrockOreVeins.TYPE, SPacketSyncBedrockOreVeins::handle);
        event.register(SPacketAddHazardZone.TYPE, SPacketAddHazardZone::handle);
        event.register(SPacketRemoveHazardZone.TYPE, SPacketRemoveHazardZone::handle);
        event.register(SPacketSyncHazardZoneStrength.TYPE, SPacketSyncHazardZoneStrength::handle);
        event.register(SPacketSyncLevelHazards.TYPE, SPacketSyncLevelHazards::handle);
        event.register(SPacketProspectOre.TYPE, SPacketProspectOre::handle);
        event.register(SPacketProspectBedrockOre.TYPE, SPacketProspectBedrockOre::handle);
        event.register(SPacketProspectBedrockFluid.TYPE, SPacketProspectBedrockFluid::handle);
    }

    public static void sendToServer(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        try {
            ClientPacketDistributor.sendToServer(payload);
        } catch (RuntimeException exception) {
            GTCEu.LOGGER.warn("Failed to send GTM payload {}", payload.type().id(), exception);
        }
    }

    public static void sendToPlayersInLevel(ResourceKey<Level> level,
                                            net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (GTCEu.getMinecraftServer() != null) {
            ServerLevel serverLevel = GTCEu.getMinecraftServer().getLevel(level);
            if (serverLevel != null) PacketDistributor.sendToPlayersInDimension(serverLevel, payload);
        }
    }

    public static void sendToAllPlayersTrackingEntity(Entity entity, boolean includeSelf,
                                                       net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (includeSelf) PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
        else PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    public static void sendToAllPlayersTrackingChunk(LevelChunk chunk,
                                                      net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (chunk.getLevel() instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, chunk.getPos(), payload);
        }
    }

    public static void sendToAll(net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    public static void sendToPlayer(ServerPlayer player,
                                    net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        if (player != null) PacketDistributor.sendToPlayer(player, payload);
    }

    public static void reply(IPayloadContext context,
                             net.minecraft.network.protocol.common.custom.CustomPacketPayload payload) {
        context.reply(payload);
    }

    /** Executes application logic on the negotiated main handler thread and contains failures. */
    public static void execute(IPayloadContext context, String payloadName, Runnable action) {
        context.enqueueWork(() -> {
            try {
                action.run();
            } catch (RuntimeException exception) {
                GTCEu.LOGGER.warn("Rejected GTM payload {} during handling", payloadName, exception);
                context.disconnect(net.minecraft.network.chat.Component.translatable("gtceu.network.invalid_payload"));
            }
        });
    }

    /** Reads a bounded non-negative count. A decoder exception rejects the connection cleanly. */
    public static int readCount(FriendlyByteBuf buffer, int maximum, String field) {
        int count = buffer.readVarInt();
        if (count < 0 || count > maximum) {
            throw new IllegalArgumentException("Invalid " + field + " count " + count + ", maximum is " + maximum);
        }
        return count;
    }

    public static String readString(FriendlyByteBuf buffer, int maximumBytes, String field) {
        try {
            return buffer.readUtf(maximumBytes);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid " + field + " string", exception);
        }
    }

    public static byte[] readBytes(FriendlyByteBuf buffer, int maximum, String field) {
        int length = buffer.readVarInt();
        if (length < 0 || length > maximum || length > buffer.readableBytes()) {
            throw new IllegalArgumentException("Invalid " + field + " byte length " + length);
        }
        byte[] bytes = new byte[length];
        buffer.readBytes(bytes);
        return bytes;
    }
}
