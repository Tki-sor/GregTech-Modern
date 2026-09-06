package com.gregtechceu.gtceu.api.transfer;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * The only compatibility boundary between GTM's existing handler algorithms
 * and NeoForge's transaction-based transfer API.
 */
public final class GTMTransferAdapters {

    private GTMTransferAdapters() {}

    public static ResourceHandler<ItemResource> item(IItemHandler handler) {
        return handler instanceof ResourceHandler<?> resourceHandler ?
                castItemHandler(resourceHandler) : new ItemHandlerAdapter(handler);
    }

    public static ResourceHandler<FluidResource> fluid(IFluidHandler handler) {
        return handler instanceof ResourceHandler<?> resourceHandler ?
                castFluidHandler(resourceHandler) : new FluidHandlerAdapter(handler);
    }

    public static EnergyHandler energy(IEnergyStorage storage) {
        return storage instanceof EnergyHandler energyHandler ? energyHandler : new EnergyStorageAdapter(storage);
    }

    public static EnergyHandler energy(IEnergyContainer container) {
        return container instanceof EnergyHandler energyHandler ? energyHandler : new EnergyContainerAdapter(container);
    }

    /**
     * Compatibility view for integrations which still expose the deprecated
     * slot API. The capability boundary itself must use {@link ResourceHandler}.
     */
    public static IItemHandler itemHandler(ResourceHandler<ItemResource> handler) {
        return IItemHandler.of(handler);
    }

    /** Compatibility view for integrations which still consume IFluidHandler. */
    public static IFluidHandler fluidHandler(ResourceHandler<FluidResource> handler) {
        return IFluidHandler.of(handler);
    }

    /** Compatibility view for integrations which still consume IEnergyStorage. */
    public static IEnergyStorage energyStorage(EnergyHandler handler) {
        return IEnergyStorage.of(handler);
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandler<ItemResource> castItemHandler(ResourceHandler<?> handler) {
        return (ResourceHandler<ItemResource>) handler;
    }

    @SuppressWarnings("unchecked")
    private static ResourceHandler<FluidResource> castFluidHandler(ResourceHandler<?> handler) {
        return (ResourceHandler<FluidResource>) handler;
    }

    private static final class ItemHandlerAdapter implements ResourceHandler<ItemResource> {

        private final IItemHandler delegate;
        private final @Nullable ItemSnapshotJournal journal;

        private ItemHandlerAdapter(IItemHandler delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            this.journal = delegate instanceof net.neoforged.neoforge.items.IItemHandlerModifiable modifiable ?
                    new ItemSnapshotJournal(modifiable) : null;
        }

        @Override
        public int size() {
            return delegate.getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(delegate.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return delegate.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return delegate.getSlotLimit(index);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.isEmpty() || delegate.isItemValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0 || !delegate.isItemValid(index, resource.toStack())) return 0;
            ItemStack input = resource.toStack(amount);
            ItemStack remainder = delegate.insertItem(index, input, true);
            int accepted = amount - remainder.getCount();
            if (accepted > 0 && journal != null) {
                journal.updateSnapshots(transaction);
                delegate.insertItem(index, input.copyWithCount(accepted), false);
            }
            return accepted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            ItemStack extracted = delegate.extractItem(index, amount, true);
            if (extracted.isEmpty() || !ItemResource.of(extracted).equals(resource)) return 0;
            if (journal != null) {
                journal.updateSnapshots(transaction);
                delegate.extractItem(index, extracted.getCount(), false);
            }
            return extracted.getCount();
        }
    }

    private static final class ItemSnapshotJournal extends SnapshotJournal<ItemStack[]> {

        private final net.neoforged.neoforge.items.IItemHandlerModifiable delegate;

        private ItemSnapshotJournal(net.neoforged.neoforge.items.IItemHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] snapshot = new ItemStack[delegate.getSlots()];
            for (int i = 0; i < snapshot.length; i++) snapshot[i] = delegate.getStackInSlot(i).copy();
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < snapshot.length && i < delegate.getSlots(); i++) {
                delegate.setStackInSlot(i, snapshot[i]);
            }
        }
    }

    private static final class FluidHandlerAdapter implements ResourceHandler<FluidResource> {

        private final IFluidHandler delegate;
        private final @Nullable FluidSnapshotJournal journal;

        private FluidHandlerAdapter(IFluidHandler delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            this.journal = delegate instanceof IFluidHandlerModifiable modifiable ?
                    new FluidSnapshotJournal(modifiable) : null;
        }

        @Override
        public int size() {
            return delegate.getTanks();
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.of(delegate.getFluidInTank(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return delegate.getFluidInTank(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return delegate.getTankCapacity(index);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.isEmpty() || delegate.isFluidValid(index, resource.toStack());
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            FluidStack stack = resource.toStack(amount);
            int accepted = delegate.fill(stack, IFluidHandler.FluidAction.SIMULATE);
            if (accepted > 0 && journal != null) {
                journal.updateSnapshots(transaction);
                delegate.fill(stack.copyWithAmount(accepted), IFluidHandler.FluidAction.EXECUTE);
            }
            return accepted;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (resource.isEmpty() || amount <= 0) return 0;
            FluidStack extracted = delegate.drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
            if (extracted.isEmpty() || !FluidResource.of(extracted).equals(resource)) return 0;
            if (journal != null) {
                journal.updateSnapshots(transaction);
                delegate.drain(extracted, IFluidHandler.FluidAction.EXECUTE);
            }
            return extracted.getAmount();
        }
    }

    private static final class FluidSnapshotJournal extends SnapshotJournal<FluidStack[]> {

        private final IFluidHandlerModifiable delegate;

        private FluidSnapshotJournal(IFluidHandlerModifiable delegate) {
            this.delegate = delegate;
        }

        @Override
        protected FluidStack[] createSnapshot() {
            FluidStack[] snapshot = new FluidStack[delegate.getTanks()];
            for (int i = 0; i < snapshot.length; i++) snapshot[i] = delegate.getFluidInTank(i).copy();
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(FluidStack[] snapshot) {
            for (int i = 0; i < snapshot.length && i < delegate.getTanks(); i++) {
                delegate.setFluidInTank(i, snapshot[i]);
            }
        }
    }

    private static final class EnergyStorageAdapter implements EnergyHandler {

        private final IEnergyStorage delegate;
        private final EnergyStorageSnapshotJournal journal;

        private EnergyStorageAdapter(IEnergyStorage delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            this.journal = new EnergyStorageSnapshotJournal(delegate);
        }

        @Override
        public long getAmountAsLong() {
            return delegate.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return delegate.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            if (amount <= 0) return 0;
            journal.updateSnapshots(transaction);
            return delegate.receiveEnergy(amount, false);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            if (amount <= 0) return 0;
            journal.updateSnapshots(transaction);
            return delegate.extractEnergy(amount, false);
        }
    }

    private static final class EnergyStorageSnapshotJournal extends SnapshotJournal<Integer> {

        private final IEnergyStorage delegate;

        private EnergyStorageSnapshotJournal(IEnergyStorage delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Integer createSnapshot() {
            return delegate.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Integer snapshot) {
            int current = delegate.getEnergyStored();
            if (current < snapshot) {
                delegate.receiveEnergy(snapshot - current, false);
            } else if (current > snapshot) {
                delegate.extractEnergy(current - snapshot, false);
            }
        }
    }

    private static final class EnergyContainerAdapter implements EnergyHandler {

        private final IEnergyContainer delegate;
        private final EnergySnapshotJournal journal;

        private EnergyContainerAdapter(IEnergyContainer delegate) {
            this.delegate = Objects.requireNonNull(delegate);
            this.journal = new EnergySnapshotJournal(delegate);
        }

        @Override
        public long getAmountAsLong() {
            return delegate.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return delegate.getEnergyCapacity();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            if (amount <= 0) return 0;
            journal.updateSnapshots(transaction);
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, delegate.changeEnergy(amount)));
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            if (amount <= 0) return 0;
            journal.updateSnapshots(transaction);
            return (int) Math.max(0, Math.min(Integer.MAX_VALUE, -delegate.changeEnergy(-amount)));
        }
    }

    private static final class EnergySnapshotJournal extends SnapshotJournal<Long> {

        private final IEnergyContainer delegate;

        private EnergySnapshotJournal(IEnergyContainer delegate) {
            this.delegate = delegate;
        }

        @Override
        protected Long createSnapshot() {
            return delegate.getEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            delegate.changeEnergy(snapshot - delegate.getEnergyStored());
        }
    }
}
