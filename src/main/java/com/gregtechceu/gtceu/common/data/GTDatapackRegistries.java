package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

/** Registers GTM world-generation definitions as codec-backed datapack registries. */
public final class GTDatapackRegistries {

    private GTDatapackRegistries() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(GTDatapackRegistries::registerDatapackRegistries);
    }

    private static void registerDatapackRegistries(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(GTRegistries.Keys.ORE_VEIN, GTOreDefinition.FULL_CODEC);
        event.dataPackRegistry(GTRegistries.Keys.BEDROCK_ORE, BedrockOreDefinition.FULL_CODEC);
        event.dataPackRegistry(GTRegistries.Keys.BEDROCK_FLUID, BedrockFluidDefinition.FULL_CODEC);
    }
}
