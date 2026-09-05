package com.gregtechceu.gtceu.api.registry;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.data.DimensionMarker;
import com.gregtechceu.gtceu.api.data.chemical.Element;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconSet;
import com.gregtechceu.gtceu.api.data.chemical.material.registry.MaterialRegistry;
import com.gregtechceu.gtceu.api.data.medicalcondition.MedicalCondition;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.IWorldGenLayer;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.placeholder.Placeholder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.category.GTRecipeCategory;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.recipe.condition.RecipeConditionType;
import com.gregtechceu.gtceu.api.sound.SoundEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import org.jetbrains.annotations.ApiStatus;

public final class GTRegistries {

    // spotless:off

    // Material related registries

    // spotless:off

    public static final MaterialRegistry MATERIALS = new MaterialRegistry();
    public static final GTRegistry.RL<Element> ELEMENTS = new GTRegistry.RL<>(GTCEu.id("element"));
    public static final GTRegistry.RL<TagPrefix> TAG_PREFIXES = new GTRegistry.RL<>(GTCEu.id("tag_prefix"));
    public static final GTRegistry.RL<MaterialIconSet> MATERIAL_ICON_SETS = new GTRegistry.RL<>(GTCEu.id("material_icon_set"));

    // Recipe related registries

    public static final GTRegistry.RL<GTRecipeType> RECIPE_TYPES = new GTRegistry.RL<>(GTCEu.id("recipe_type"));
    public static final GTRegistry.RL<GTRecipeCategory> RECIPE_CATEGORIES = new GTRegistry.RL<>(GTCEu.id("recipe_category"));
    public static final GTRegistry.RL<RecipeCapability<?>> RECIPE_CAPABILITIES = new GTRegistry.RL<>(GTCEu.id("recipe_capability"));
    public static final GTRegistry.RL<RecipeConditionType<?>> RECIPE_CONDITIONS = new GTRegistry.RL<>(GTCEu.id("recipe_condition"));
    public static final GTRegistry.RL<ChanceLogic> CHANCE_LOGICS = new GTRegistry.RL<>(GTCEu.id("chance_logic"));

    // Worldgen related registries

    public static final GTRegistry.RL<BedrockFluidDefinition> BEDROCK_FLUID_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_fluid"));
    public static final GTRegistry.RL<BedrockOreDefinition> BEDROCK_ORE_DEFINITIONS = new GTRegistry.RL<>(GTCEu.id("bedrock_ore"));
    public static final GTRegistry.RL<GTOreDefinition> ORE_VEINS = new GTRegistry.RL<>(GTCEu.id("ore_vein"));
    public static final GTRegistry.RL<IWorldGenLayer> WORLD_GEN_LAYERS = new GTRegistry.RL<>(GTCEu.id("world_gen_layer"));

    // Other registries

    public static final GTRegistry.RL<CoverDefinition> COVERS = new GTRegistry.RL<>(GTCEu.id("cover"));
    public static final GTRegistry.RL<MachineDefinition> MACHINES = new GTRegistry.RL<>(GTCEu.id("machine"));
    public static final GTRegistry.RL<SoundEntry> SOUNDS = new GTRegistry.RL<>(GTCEu.id("sound"));
    public static final GTRegistry.RL<DimensionMarker> DIMENSION_MARKERS = new GTRegistry.RL<>(GTCEu.id("dimension_marker"));
    public static final GTRegistry.RL<MedicalCondition> MEDICAL_CONDITIONS = new GTRegistry.RL<>(GTCEu.id("medical_condition"));
    public static final GTRegistry.RL<Placeholder> PLACEHOLDERS = new GTRegistry.RL<>(GTCEu.id("placeholder"));
    public static final GTRegistry.RL<PatternError.PatternErrorType> PATTERN_ERRORS = new GTRegistry.RL<>(
            GTCEu.id("pattern_errors"));

    private static final Map<ResourceKey<? extends Registry<?>>, GTRegistry<?, ?>> CUSTOM_REGISTRIES = new LinkedHashMap<>();
    private static final Map<ResourceKey<? extends Registry<?>>, Registry<?>> NEOFORGE_REGISTRIES = new LinkedHashMap<>();
    private static final Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Object>> PENDING_REGISTRATIONS = new LinkedHashMap<>();

    static {
        registerBacking(MATERIALS);
        registerBacking(ELEMENTS);
        registerBacking(TAG_PREFIXES);
        registerBacking(MATERIAL_ICON_SETS);
        registerBacking(RECIPE_TYPES);
        registerBacking(RECIPE_CATEGORIES);
        registerBacking(RECIPE_CAPABILITIES);
        registerBacking(RECIPE_CONDITIONS);
        registerBacking(CHANCE_LOGICS);
        registerBacking(BEDROCK_FLUID_DEFINITIONS);
        registerBacking(BEDROCK_ORE_DEFINITIONS);
        registerBacking(ORE_VEINS);
        registerBacking(WORLD_GEN_LAYERS);
        registerBacking(COVERS);
        registerBacking(MACHINES);
        registerBacking(SOUNDS);
        registerBacking(DIMENSION_MARKERS);
        registerBacking(MEDICAL_CONDITIONS);
        registerBacking(PLACEHOLDERS);
        registerBacking(PATTERN_ERRORS);
    }


    // spotless:on

    public static <V, T extends V> T register(Registry<V> registry, Identifier name, T value) {
        PENDING_REGISTRATIONS.computeIfAbsent(registry.key(), ignored -> new LinkedHashMap<>()).put(name, value);
        return value;
    }

    private static <T> void registerBacking(GTRegistry<?, T> registry) {
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(registry.getRegistryName());
        CUSTOM_REGISTRIES.put(key, registry);
        NEOFORGE_REGISTRIES.put(key, new RegistryBuilder<T>(key).sync(true).create());
    }

    /** Registers GTM's custom registries on the NeoForge mod bus. */
    public static void init(IEventBus modBus) {
        modBus.addListener(GTRegistries::registerCustomRegistries);
        modBus.addListener(GTRegistries::registerCustomEntries);
    }

    private static void registerCustomRegistries(NewRegistryEvent event) {
        NEOFORGE_REGISTRIES.values().forEach(event::register);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void registerCustomEntries(RegisterEvent event) {
        GTRegistry registry = CUSTOM_REGISTRIES.get(event.getRegistryKey());
        if (registry != null) {
            for (Object entryObject : registry.entries()) {
                Map.Entry entry = (Map.Entry) entryObject;
                event.register((ResourceKey) event.getRegistryKey(), (Identifier) entry.getKey(),
                        () -> entry.getValue());
            }
        }

        Map<Identifier, Object> pending = PENDING_REGISTRATIONS.remove(event.getRegistryKey());
        if (pending != null) {
            pending.forEach((id, value) -> event.register((ResourceKey) event.getRegistryKey(), id, () -> value));
        }
    }

    /** Returns the NeoForge registry for a GTM registry key after registration. */
    @SuppressWarnings("unchecked")
    public static <T> Registry<T> registry(Identifier registryName) {
        return (Registry<T>) NEOFORGE_REGISTRIES.entrySet().stream()
                .filter(entry -> entry.getKey().location().equals(registryName))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /** Resolves a holder from a registered GTM registry without exposing its implementation map. */
    public static <T> java.util.Optional<net.minecraft.core.Holder.Reference<T>> holder(
                                                                                        ResourceKey<Registry<T>> registryKey,
                                                                                        Identifier valueKey) {
        Registry<T> registry = registry(registryKey.location());
        return registry == null ? java.util.Optional.empty() : registry.get(valueKey);
    }

    public static Collection<Registry<?>> getRegistries() {
        return java.util.Collections.unmodifiableCollection(NEOFORGE_REGISTRIES.values());
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (GTCEu.isClientThread()) {
            return ClientHelpers.getClientRegistries();
        }
        return FROZEN;
    }

    private static class ClientHelpers {

        private static RegistryAccess getClientRegistries() {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            } else {
                return FROZEN;
            }
        }
    }
}
