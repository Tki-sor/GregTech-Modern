package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.gregtechceu.gtceu.common.network.GTNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Monitor item update. S2C, main-thread client handling, required payload. */
public final class SCPacketMonitorGroupNBTChange implements CustomPacketPayload {

    public static final Type<SCPacketMonitorGroupNBTChange> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(GTCEu.MOD_ID, "monitor_group_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SCPacketMonitorGroupNBTChange> CODEC =
            StreamCodec.ofMember(SCPacketMonitorGroupNBTChange::encode, SCPacketMonitorGroupNBTChange::decode);

    private final ItemStack stack;
    private final int monitorGroupId;
    private final BlockPos pos;

    public SCPacketMonitorGroupNBTChange(ItemStack stack, MonitorGroup group, CentralMonitorMachine machine) {
        this(stack, machine.getMonitorGroups().indexOf(group), machine.getBlockPos());
    }

    private SCPacketMonitorGroupNBTChange(ItemStack stack, int monitorGroupId, BlockPos pos) {
        this.stack = stack;
        this.monitorGroupId = monitorGroupId;
        this.pos = pos;
    }

    private static SCPacketMonitorGroupNBTChange decode(RegistryFriendlyByteBuf buffer) {
        int group = buffer.readVarInt();
        if (group < 0 || group > 100_000) throw new IllegalArgumentException("Invalid monitor group id");
        return new SCPacketMonitorGroupNBTChange(ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer), group,
                BlockPos.STREAM_CODEC.decode(buffer));
    }

    private void encode(RegistryFriendlyByteBuf buffer) {
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack);
        buffer.writeVarInt(monitorGroupId);
        BlockPos.STREAM_CODEC.encode(buffer, pos);
    }

    public static void handle(SCPacketMonitorGroupNBTChange packet, IPayloadContext context) {
        GTNetwork.execute(context, TYPE.id().toString(), () -> {
            if (packet.monitorGroupId < 0 || context.player() == null) return;
            MetaMachine machine = MetaMachine.getMachine(context.player().level(), packet.pos);
            if (machine instanceof CentralMonitorMachine centralMonitor &&
                    packet.monitorGroupId < centralMonitor.getMonitorGroups().size()) {
                IItemHandlerModifiable itemHandler = centralMonitor.getMonitorGroups().get(packet.monitorGroupId)
                        .getItemStackHandler();
                if (ItemStack.isSameItem(itemHandler.getStackInSlot(0), packet.stack)) {
                    itemHandler.setStackInSlot(0, packet.stack);
                }
            }
        });
    }

    @Override
    public Type<SCPacketMonitorGroupNBTChange> type() {
        return TYPE;
    }
}
