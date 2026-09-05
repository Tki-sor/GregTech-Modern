package brachy.modularui.network;

import brachy.modularui.ModularUI;
import brachy.modularui.network.packets.CloseAllGuisPacket;
import brachy.modularui.network.packets.CloseGuiPacket;
import brachy.modularui.network.packets.OpenGuiPacket;
import brachy.modularui.network.packets.ReopenGuiPacket;
import brachy.modularui.network.packets.SyncHandlerPacket;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

/** Client-only payload handlers. The common MUI registrar deliberately leaves these unbound. */
@EventBusSubscriber(modid = ModularUI.MOD_ID, value = Dist.CLIENT)
public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    @SubscribeEvent
    public static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(OpenGuiPacket.TYPE, NetworkHandler.safe(OpenGuiPacket::execute));
        event.register(SyncHandlerPacket.TYPE, NetworkHandler.safe(SyncHandlerPacket::execute));
        event.register(CloseAllGuisPacket.TYPE, NetworkHandler.safe(CloseAllGuisPacket::execute));
        event.register(CloseGuiPacket.TYPE, NetworkHandler.safe(CloseGuiPacket::execute));
        event.register(ReopenGuiPacket.TYPE, NetworkHandler.safe(ReopenGuiPacket::execute));
    }
}
