# Task 11 Data and Datagen

## Implemented

- Migrated GTM item ingredients to NeoForge `ICustomIngredient`, `IngredientType`, `MapCodec`, and registry-aware `StreamCodec` boundaries. Sized, ranged, circuit, NBT-predicate, and fluid-container ingredients retain their matching and display semantics through vanilla `Ingredient` wrappers.
- Reworked GTM recipe serializers and custom crafting recipes for the 26.1 `RecipeSerializer` record (`MapCodec` plus `StreamCodec`) and `CraftingInput` contracts.
- Split data generation into `GatherDataEvent.Client` and `GatherDataEvent.Server`, using `createProvider` and `createDatapackRegistryObjects` with registry lookup futures.
- Registered loot functions and conditions as direct `MapCodec` registry entries and updated loot table/modifier providers to the lookup-aware 26.1 contracts.
- Registered ore veins, bedrock ores, and bedrock fluids as codec-backed NeoForge datapack registries. Composter entries now use NeoForge's compostable data map and a data-map provider.
- Replaced the removed vanilla `FinishedRecipe` dependency in GTM recipe generation with a small internal serialized-recipe boundary used by the dynamic data pack writer.

## Verification

`./gradlew --no-daemon compileJava` was run with JDK 25. Compilation reaches the existing later-task diagnostics. The remaining visible failures are transfer/capability, networking/MUI codec, client rendering, and optional integration migration groups; no task-11-specific diagnostic remains in the compiler's capped output.

`runData` was attempted with the same toolchain and is blocked by the same compile failure before the data run can start.
