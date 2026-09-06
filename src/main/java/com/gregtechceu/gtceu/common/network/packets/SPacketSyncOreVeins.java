package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.client.ClientProxy;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

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

/** Legacy datapack sync retained only as a bounded S2C compatibility payload for this port. */
public record SPacketSyncOreVeins(Map<Identifier, GTOreDefinition> veins) implements CustomPacketPayload {

    public static final Type<SPacketSyncOreVeins> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "ore_veins_sync"));
    public static final StreamCodec<FriendlyByteBuf, SPacketSyncOreVeins> CODEC = StreamCodec.ofMember(
            SPacketSyncOreVeins::encode, SPacketSyncOreVeins::decode);

    private static SPacketSyncOreVeins decode(FriendlyByteBuf buffer) {
        int count = GTNetwork.readCount(buffer, GTNetwork.MAX_COLLECTION_ENTRIES, "ore vein");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        Map<Identifier, GTOreDefinition> values = new LinkedHashMap<>(count);
        for (int i = 0; i < count; i++) {
            Identifier id = buffer.readIdentifier();
            CompoundTag tag = buffer.readNbt();
            if (tag == null) throw new IllegalArgumentException("Missing ore vein definition");
            values.put(id, GTOreDefinition.FULL_CODEC.parse(ops, tag)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid ore vein " + id + ": " + message)));
        }
        return new SPacketSyncOreVeins(values);
    }

    private void encode(FriendlyByteBuf buffer) {
        if (veins.size() > GTNetwork.MAX_COLLECTION_ENTRIES) throw new IllegalArgumentException("Too many ore veins");
        RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, GTRegistries.builtinRegistry());
        buffer.writeVarInt(veins.size());
        veins.forEach((id, definition) -> {
            buffer.writeIdentifier(id);
            CompoundTag tag = (CompoundTag) GTOreDefinition.FULL_CODEC.encodeStart(ops, definition)
                    .getOrThrow(message -> new IllegalArgumentException("Invalid ore vein " + id + ": " + message));
            buffer.writeNbt(tag);
        });
    }

    public static void handle(SPacketSyncOreVeins packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            ClientProxy.CLIENT_ORE_VEINS.clear();
            ClientProxy.CLIENT_ORE_VEINS.putAll(packet.veins());
            GTClientCache.instance.oreVeinDefinitionsChanged(ClientProxy.CLIENT_ORE_VEINS);
        });
    }

    @Override
    public Type<SPacketSyncOreVeins> type() {
        return TYPE;
    }
}
