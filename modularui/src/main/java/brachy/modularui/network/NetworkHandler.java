package brachy.modularui.network;

import brachy.modularui.ModularUI;
import brachy.modularui.network.packets.CloseAllGuisPacket;
import brachy.modularui.network.packets.CloseGuiPacket;
import brachy.modularui.network.packets.OpenGuiPacket;
import brachy.modularui.network.packets.ReopenGuiPacket;
import brachy.modularui.network.packets.SyncHandlerPacket;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.chat.Component;

@EventBusSubscriber(modid = ModularUI.MOD_ID)
public class NetworkHandler {

    public static final String NETWORK_VERSION = "1.0.0";

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(NETWORK_VERSION).executesOn(HandlerThread.MAIN);

        registrar.playBidirectional(OpenGuiPacket.TYPE, OpenGuiPacket.CODEC, safe(OpenGuiPacket::execute));
        registrar.playBidirectional(SyncHandlerPacket.TYPE, SyncHandlerPacket.CODEC, safe(SyncHandlerPacket::execute));
        registrar.playBidirectional(CloseAllGuisPacket.TYPE, CloseAllGuisPacket.CODEC,
                safe(CloseAllGuisPacket::execute));
        registrar.playBidirectional(CloseGuiPacket.TYPE, CloseGuiPacket.CODEC, safe(CloseGuiPacket::execute));
        registrar.playBidirectional(ReopenGuiPacket.TYPE, ReopenGuiPacket.CODEC, safe(ReopenGuiPacket::execute));
    }

    public static <T extends CustomPacketPayload> IPayloadHandler<T> safe(IPayloadHandler<T> handler) {
        return (payload, context) -> context.enqueueWork(() -> {
            try {
                handler.handle(payload, context);
            } catch (RuntimeException exception) {
                ModularUI.LOGGER.warn("Rejected MUI payload {}", payload.type().id(), exception);
                context.disconnect(Component.translatable("modularui.network.invalid_payload"));
            }
        });
    }

}
