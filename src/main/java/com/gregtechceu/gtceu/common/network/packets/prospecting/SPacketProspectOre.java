package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.ores.GeneratedVeinMetadata;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;

public final class SPacketProspectOre extends SPacketProspect<GeneratedVeinMetadata> {

    public static final Type<SPacketProspectOre> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "prospect_ore"));
    public static final StreamCodec<FriendlyByteBuf, SPacketProspectOre> CODEC = StreamCodec.ofMember(
            SPacketProspectOre::encode, SPacketProspectOre::new);

    private SPacketProspectOre(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public SPacketProspectOre(ResourceKey<Level> key, Collection<GeneratedVeinMetadata> veins) {
        super(key, veins.stream().map(GeneratedVeinMetadata::center).toList(), veins);
    }

    private void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
    }

    @Override
    protected void encodeData(FriendlyByteBuf buffer, GeneratedVeinMetadata value) {
        value.writeToPacket(buffer);
    }

    @Override
    protected GeneratedVeinMetadata decodeData(FriendlyByteBuf buffer) {
        return GeneratedVeinMetadata.readFromPacket(buffer);
    }

    public static void handle(SPacketProspectOre packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> packet.data.rowMap().forEach((level, ores) -> ores
                .forEach((blockPos, vein) -> GTClientCache.instance.addVein(level,
                        blockPos.getX() >> 4, blockPos.getZ() >> 4, vein))));
    }

    @Override
    public Type<SPacketProspectOre> type() {
        return TYPE;
    }
}
