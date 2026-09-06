package com.gregtechceu.gtceu.api.transfer.fluid;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CustomFluidTank extends FluidTank implements IFluidHandlerModifiable, INBTSerializable<CompoundTag>,
        ResourceHandler<FluidResource> {

    private final SnapshotJournal<FluidStack> transactionJournal = new SnapshotJournal<>() {
        @Override
        protected FluidStack createSnapshot() {
            return getFluid().copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            setFluid(snapshot);
        }
    };

    @Getter
    @Setter
    protected @NotNull Runnable onContentsChanged = () -> {};

    public CustomFluidTank(int capacity) {
        super(capacity, e -> true);
    }

    public CustomFluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    public CustomFluidTank(FluidStack stack) {
        super(stack.getAmount());
        setFluid(stack);
    }

    @Override
    protected void onContentsChanged() {
        onContentsChanged.run();
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        setFluid(stack);
    }

    @Override
    public void setFluid(FluidStack stack) {
        super.setFluid(stack);
        this.onContentsChanged();
    }

    @Override
    public CompoundTag serializeNBT() {
        var tag = new CompoundTag();
        if (isEmpty() || getFluidAmount() <= 0) tag.putBoolean("isNull", true);
        return writeToNBT(tag);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.getBoolean("isNull")) return;
        readFromNBT(nbt);
    }

    @Override
    public int size() {
        return getTanks();
    }

    @Override
    public FluidResource getResource(int index) {
        return FluidResource.of(getFluidInTank(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getFluidInTank(index).getAmount();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        return getTankCapacity(index);
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        return resource.isEmpty() || isFluidValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        FluidStack input = resource.toStack(amount);
        int inserted = fill(input, IFluidHandler.FluidAction.SIMULATE);
        if (inserted > 0) {
            transactionJournal.updateSnapshots(transaction);
            fill(input.copyWithAmount(inserted), IFluidHandler.FluidAction.EXECUTE);
        }
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        FluidStack current = getFluidInTank(index);
        if (current.isEmpty() || !FluidResource.of(current).equals(resource)) return 0;
        FluidStack extracted = drain(resource.toStack(amount), IFluidHandler.FluidAction.SIMULATE);
        if (extracted.isEmpty()) return 0;
        transactionJournal.updateSnapshots(transaction);
        drain(extracted, IFluidHandler.FluidAction.EXECUTE);
        return extracted.getAmount();
    }
}
