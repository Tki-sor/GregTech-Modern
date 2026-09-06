package com.gregtechceu.gtceu.api.item;

import com.gregtechceu.gtceu.api.data.serialization.INBTSerializable;

import net.minecraft.nbt.Tag;

import net.minecraft.world.item.ItemStack;

/**
 * An interface for per-stack capability objects that store NBT data and need custom comparison
 * logic when two stacks of the same item are compared or merged.
 */
public interface IMergeableNBTSerializable extends INBTSerializable<Tag> {

    /**
     * Called right before this object is compared to a different one during custom stack
     * comparison or merging.
     * The other object is guaranteed to have the same id as this one, but may be {@code null} if
     * the other item does not have this capability.
     *
     * @param other the other capability provider
     */
    void prepareForComparisonWith(INBTSerializable<Tag> other);
}
