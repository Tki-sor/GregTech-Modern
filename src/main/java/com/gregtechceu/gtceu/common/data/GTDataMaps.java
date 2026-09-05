package com.gregtechceu.gtceu.common.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import java.util.concurrent.CompletableFuture;

/** GTM's data-driven replacements for vanilla static registry maps. */
public final class GTDataMaps {

    private GTDataMaps() {}

    public static final class Provider extends DataMapProvider {
        public Provider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
            super(output, lookup);
        }

        @Override
        protected void gather(HolderLookup.Provider provider) {
            builder(NeoForgeDataMaps.COMPOSTABLES)
                    .add(GTBlocks.RUBBER_LEAVES.asItem().builtInRegistryHolder(), new Compostable(0.3F), false)
                    .add(GTBlocks.RUBBER_SAPLING.asItem().builtInRegistryHolder(), new Compostable(0.3F), false);
        }
    }
}
