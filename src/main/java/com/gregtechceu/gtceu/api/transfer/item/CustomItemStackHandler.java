package com.gregtechceu.gtceu.api.transfer.item;

import com.gregtechceu.gtceu.api.data.serialization.INBTSerializable;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class CustomItemStackHandler extends ItemStackHandler
                                    implements INBTSerializable<CompoundTag>, ResourceHandler<ItemResource> {

    private final SnapshotJournal<ItemStack[]> transactionJournal = new SnapshotJournal<>() {
        @Override
        protected ItemStack[] createSnapshot() {
            ItemStack[] snapshot = new ItemStack[getSlots()];
            for (int i = 0; i < snapshot.length; i++) snapshot[i] = getStackInSlot(i).copy();
            return snapshot;
        }

        @Override
        protected void revertToSnapshot(ItemStack[] snapshot) {
            for (int i = 0; i < snapshot.length && i < getSlots(); i++) setStackInSlot(i, snapshot[i]);
        }
    };

    @Getter
    @Setter
    protected @NotNull Runnable onContentsChanged = () -> {};
    @Getter
    @Setter
    protected Predicate<ItemStack> filter = stack -> true;

    public CustomItemStackHandler() {
        super();
    }

    public CustomItemStackHandler(int size) {
        super(size);
    }

    public CustomItemStackHandler(ItemStack itemStack) {
        this(NonNullList.of(ItemStack.EMPTY, itemStack));
    }

    public CustomItemStackHandler(NonNullList<ItemStack> stacks) {
        super(stacks);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return filter.test(stack);
    }

    @Override
    public void onContentsChanged(int slot) {
        onContentsChanged.run();
    }

    public void clear() {
        stacks.clear();
        onContentsChanged.run();
    }

    public NonNullList<ItemStack> toList() {
        NonNullList<ItemStack> list = NonNullList.create();
        for (int slot = 0; slot < getSlots(); slot++) list.add(getStackInSlot(slot));
        return list;
    }

    public void dropInventoryInWorld(Level world, BlockPos pos) {
        for (ItemStack stack : stacks) {
            Block.popResource(world, pos, stack);
        }
        clear();
    }

    @Override
    public CompoundTag serializeNBT() {
        TagValueOutput output = TagValueOutput.createWithContext(new ProblemReporter.Collector(),
                GTRegistries.builtinRegistry());
        serialize(output);
        return output.buildResult();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        deserialize(TagValueInput.create(new ProblemReporter.Collector(), GTRegistries.builtinRegistry(), nbt));
    }

    @Override
    public int size() {
        return getSlots();
    }

    @Override
    public ItemResource getResource(int index) {
        return ItemResource.of(getStackInSlot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return getStackInSlot(index).getCount();
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return getSlotLimit(index);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return resource.isEmpty() || isItemValid(index, resource.toStack());
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        ItemStack input = resource.toStack(amount);
        ItemStack remainder = insertItem(index, input, true);
        int inserted = amount - remainder.getCount();
        if (inserted > 0) {
            transactionJournal.updateSnapshots(transaction);
            insertItem(index, input.copyWithCount(inserted), false);
        }
        return inserted;
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        if (resource.isEmpty() || amount <= 0) return 0;
        ItemStack current = getStackInSlot(index);
        if (current.isEmpty() || !ItemResource.of(current).equals(resource)) return 0;
        ItemStack extracted = extractItem(index, amount, true);
        if (extracted.isEmpty()) return 0;
        transactionJournal.updateSnapshots(transaction);
        extractItem(index, extracted.getCount(), false);
        return extracted.getCount();
    }
}
