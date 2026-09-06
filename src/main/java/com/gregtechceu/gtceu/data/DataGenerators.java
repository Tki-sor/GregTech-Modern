package com.gregtechceu.gtceu.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.SoundEntryBuilder;
import com.gregtechceu.gtceu.common.data.GTDamageTypes;
import com.gregtechceu.gtceu.common.data.worldgen.GTBiomeModifiers;
import com.gregtechceu.gtceu.common.data.worldgen.GTConfiguredFeatures;
import com.gregtechceu.gtceu.common.data.worldgen.GTDensityFunctions;
import com.gregtechceu.gtceu.common.data.worldgen.GTPlacedFeatures;
import com.gregtechceu.gtceu.data.loot.GTLootModifications;
import com.gregtechceu.gtceu.data.loot.GTLootTables;
import com.gregtechceu.gtceu.data.tags.BiomeTagsLoader;
import com.gregtechceu.gtceu.data.tags.DamageTagsLoader;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.ForgeRegistries;

import java.util.Set;

@EventBusSubscriber(modid = GTCEu.MOD_ID)
public final class DataGenerators {

    private DataGenerators() {}

    @SubscribeEvent
    public static void gatherClient(GatherDataEvent.Client event) {
        event.createProvider(output -> new SoundEntryBuilder.SoundEntryProvider(output, GTCEu.MOD_ID));
    }

    @SubscribeEvent
    public static void gatherServer(GatherDataEvent.Server event) {
        var lookup = event.getLookupProvider();
        event.createProvider(output -> new BiomeTagsLoader(output, lookup));
        event.createDatapackRegistryObjects(new RegistrySetBuilder()
                .add(Registries.DAMAGE_TYPE, GTDamageTypes::bootstrap)
                .add(Registries.CONFIGURED_FEATURE, GTConfiguredFeatures::bootstrap)
                .add(Registries.PLACED_FEATURE, GTPlacedFeatures::bootstrap)
                .add(Registries.DENSITY_FUNCTION, GTDensityFunctions::bootstrap)
                .add(ForgeRegistries.Keys.BIOME_MODIFIERS, GTBiomeModifiers::bootstrap), Set.of(GTCEu.MOD_ID));
        var moddedLookup = event.getLookupProvider();
        event.createProvider(output -> new com.gregtechceu.gtceu.common.data.GTDataMaps.Provider(output, moddedLookup));
        event.createProvider(output -> new DamageTagsLoader(output, moddedLookup));
        event.createProvider(output -> new GTLootTables(output, moddedLookup));
        event.createProvider(output -> new GTLootModifications(output, moddedLookup));
    }
}
