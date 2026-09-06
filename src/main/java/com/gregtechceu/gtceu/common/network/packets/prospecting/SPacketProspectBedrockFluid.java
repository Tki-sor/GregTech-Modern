package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.integration.map.cache.client.GTClientCache;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Collection;

public final class SPacketProspectBedrockFluid extends SPacketProspect<ProspectorMode.FluidInfo> {

    public static final Type<SPacketProspectBedrockFluid> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "prospect_bedrock_fluid"));
    public static final StreamCodec<FriendlyByteBuf, SPacketProspectBedrockFluid> CODEC = StreamCodec.ofMember(
            SPacketProspectBedrockFluid::encode, SPacketProspectBedrockFluid::new);

    private SPacketProspectBedrockFluid(FriendlyByteBuf buffer) {
        super(buffer);
    }

    public SPacketProspectBedrockFluid(ResourceKey<Level> key, Collection<BlockPos> positions,
                                       Collection<ProspectorMode.FluidInfo> prospected) {
        super(key, positions, prospected);
    }

    public SPacketProspectBedrockFluid(ResourceKey<Level> key, BlockPos pos, ProspectorMode.FluidInfo vein) {
        super(key, pos, vein);
    }

    private void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
    }

    @Override
    protected void encodeData(FriendlyByteBuf buffer, ProspectorMode.FluidInfo value) {
        ProspectorMode.FLUID.serialize(value, buffer);
    }

    @Override
    protected ProspectorMode.FluidInfo decodeData(FriendlyByteBuf buffer) {
        return ProspectorMode.FLUID.deserialize(buffer);
    }

    public static void handle(SPacketProspectBedrockFluid packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> packet.data.rowMap().forEach((level, fluids) -> fluids
                .forEach((blockPos, fluid) -> GTClientCache.instance.addFluid(level,
                        blockPos.getX() >> 4, blockPos.getZ() >> 4, fluid))));
    }

    @Override
    public Type<SPacketProspectBedrockFluid> type() {
        return TYPE;
    }
}
