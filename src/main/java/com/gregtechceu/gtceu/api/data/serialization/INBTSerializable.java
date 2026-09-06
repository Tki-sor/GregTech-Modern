package com.gregtechceu.gtceu.api.data.serialization;

import net.minecraft.nbt.Tag;

/**
 * GTM-owned replacement for NeoForge's removed {@code INBTSerializable}.
 *
 * <p>NeoForge 26.1 removed {@code net.neoforged.neoforge.common.util.INBTSerializable} in favour of
 * {@code ValueIOSerializable} (ValueInput/ValueOutput). GTM's internal save and sync pipelines are still
 * tag-based ({@code ManagedSyncBlockEntity}, pipe nets, virtual registries), so handlers and objects that
 * participate in those pipelines keep the historical no-provider {@code serializeNBT()} /
 * {@code deserializeNBT(CompoundTag)} convention through this interface instead.
 */
public interface INBTSerializable<T extends Tag> {

    T serializeNBT();

    void deserializeNBT(T nbt);
}
