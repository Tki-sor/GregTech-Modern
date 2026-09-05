package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.LinkedHashMap;
import java.util.Map;

public record SPacketSyncBedrockOreVeins(Map<Identifier, BedrockOreDefinition> veins) implements CustomPacketPayload {

    public static final Type<SPacketSyncBedrockOreVeins> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "bedrock_ore_veins_sync"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncBedrockOreVeins> CODEC = StreamCodec.ofMember(
            SPacketSyncBedrockOreVeins::encode, SPacketSyncBedrockOreVeins::decode);

    private static SPacketSyncBedrockOreVeins decode(FriendlyByteBuf buffer) {
        int count = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "bedrock ore");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        Map<Identifier, BedrockOreDefinition> values = new LinkedHashMap<>(count);
        for (int i = 0; i < count; i++) {
            Identifier id = buffer.readIdentifier();
            CompoundTag tag = buffer.readNbt();
            if (tag == null) throw new IllegalArgumentException("Missing bedrock ore definition");
            values.put(id, BedrockOreDefinition.FULL_CODEC.parse(ops, tag)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid bedrock ore " + id + ": " + message)));
        }
        return new SPacketSyncBedrockOreVeins(values);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (veins.size() > GTNetwork.MAX_COLLECTION_ENTRIES) throw new IllegalArgumentException("Too many bedrock ores");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        buffer.writeVarInt(veins.size());
        veins.forEach((id, definition) -> {
            buffer.writeIdentifier(id);
            CompoundTag tag = (CompoundTag) BedrockOreDefinition.FULL_CODEC.encodeStart(ops, definition)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid bedrock ore " + id + ": " + message));
            buffer.writeNbt(tag);
        });
    }

    public static void handle(SPacketSyncBedrockOreVeins packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            ClientProxy.CLIENT_BEDROCK_ORE_VEINS.clear();
            ClientProxy.CLIENT_BEDROCK_ORE_VEINS.putAll(packet.veins());
        });
    }

    @Override
    public Type<SPacketSyncBedrockOreVeins> type() {
        return TYPE;
    }
}
