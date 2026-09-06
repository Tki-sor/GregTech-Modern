package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
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

public record SPacketSyncFluidVeins(Map<Identifier, BedrockFluidDefinition> veins) implements CustomPacketPayload {

    public static final Type<SPacketSyncFluidVeins> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "fluid_veins_sync"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncFluidVeins> CODEC = StreamCodec.ofMember(
            SPacketSyncFluidVeins::encode, SPacketSyncFluidVeins::decode);

    private static SPacketSyncFluidVeins decode(FriendlyByteBuf buffer) {
        int count = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "bedrock fluid");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        Map<Identifier, BedrockFluidDefinition> values = new LinkedHashMap<>(count);
        for (int i = 0; i < count; i++) {
            Identifier id = buffer.readIdentifier();
            CompoundTag tag = buffer.readNbt();
            if (tag == null) throw new IllegalArgumentException("Missing bedrock fluid definition");
            values.put(id, BedrockFluidDefinition.FULL_CODEC.parse(ops, tag)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid bedrock fluid " + id + ": " + message)));
        }
        return new SPacketSyncFluidVeins(values);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (veins.size() > GTNetwork.MAX_COLLECTION_ENTRIES) throw new IllegalArgumentException("Too many bedrock fluids");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        buffer.writeVarInt(veins.size());
        veins.forEach((id, definition) -> {
            buffer.writeIdentifier(id);
            CompoundTag tag = (CompoundTag) BedrockFluidDefinition.FULL_CODEC.encodeStart(ops, definition)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid bedrock fluid " + id + ": " + message));
            buffer.writeNbt(tag);
        });
    }

    public static void handle(SPacketSyncFluidVeins packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            ClientProxy.CLIENT_FLUID_VEINS.clear();
            ClientProxy.CLIENT_FLUID_VEINS.putAll(packet.veins());
        });
    }

    @Override
    public Type<SPacketSyncFluidVeins> type() {
        return TYPE;
    }
}
