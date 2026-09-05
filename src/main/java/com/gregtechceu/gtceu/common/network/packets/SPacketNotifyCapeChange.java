package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.cosmetics.CapeRegistry;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.NetworkDirection;
import net.neoforged.neoforge.network.NetworkEvent;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class SPacketNotifyCapeChange implements GTNetwork.INetPacket {

    public UUID uuid;
    public Identifier cape;

    public SPacketNotifyCapeChange(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        cape = buf.readBoolean() ? buf.readIdentifier() : null;
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.uuid);
        buf.writeBoolean(this.cape != null);
        if (this.cape != null) {
            buf.writeIdentifier(this.cape);
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        if (context.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            CapeRegistry.giveRawCape(uuid, cape);
        }
    }
}
