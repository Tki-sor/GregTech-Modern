package com.gregtechceu.gtceu.common.network.packets.prospecting;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.component.prospector.ProspectorMode;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SPacketProspectBedrockOre extends SPacketProspect<ProspectorMode.BedrockOreInfo> {

    public static final Type<SPacketProspectBedrockOre> TYPE = new Type<>(Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "prospect_bedrock_ore"));
    public static final StreamCodec<FriendlyByteBuf, SPacketProspectBedrockOre> CODEC = StreamCodec.ofMember(
            SPacketProspectBedrockOre::encode, SPacketProspectBedrockOre::new);

    private SPacketProspectBedrockOre(FriendlyByteBuf buffer) {
        super(buffer);
    }

    private void encode(FriendlyByteBuf buffer) {
        super.encode(buffer);
    }

    @Override
    protected void encodeData(FriendlyByteBuf buffer, ProspectorMode.BedrockOreInfo value) {
        ProspectorMode.BEDROCK_ORE.serialize(value, buffer);
    }

    @Override
    protected ProspectorMode.BedrockOreInfo decodeData(FriendlyByteBuf buffer) {
        return ProspectorMode.BEDROCK_ORE.deserialize(buffer);
    }

    public static void handle(SPacketProspectBedrockOre packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            // Bedrock ore map caching is a separate data concern; decode and reject malformed entries here.
        });
    }

    @Override
    public Type<SPacketProspectBedrockOre> type() {
        return TYPE;
    }
}
