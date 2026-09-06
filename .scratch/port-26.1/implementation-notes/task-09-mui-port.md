# Task 09 ModularUI Port

## Implemented

- Kept ModularUI-Modern `1.21.1` as the source/API baseline in the independent `:modularui` Gradle module.
- Targeted Minecraft `26.1.2`, NeoForge `26.1.2.103`, Java `25`, and official Mojang mappings.
- Preserved the `brachy.modularui` package surface and GTM's existing Jar-in-Jar project dependency boundary.
- Removed stale EMI mixin and metadata references. EMI, Embeddium, and MUI test sources remain excluded because no verified target artifacts or compatible source contracts are available; JEI and REI remain compiled integrations.
- Migrated the MUI component implementation away from final `MutableComponent` inheritance and onto the target `Component` contract, including siblings, style, visual-order caching, copies, and component construction.
- Migrated client reload registration, tooltip extraction, GUI sprite atlas lookup, item/entity/text extraction, input records, packet distribution, registry access, fluid identity, crafting signatures, and target render-pipeline construction.
- Replaced removed immediate GUI buffer access with `GuiGraphicsExtractor` fills and submitted `GuiElementRenderState` geometry where applicable.
- Replaced the removed immediate schema chunk renderer with a registered NeoForge Picture-in-Picture renderer. Schema blocks use target `ModelBlockRenderer` tessellation, fluids use target `FluidRenderer` tessellation, block entities use `BlockEntityRenderDispatcher` render-state extraction/submission, outlines use target `ShapeRenderer`, and the scene renders into the PIP color/depth target before being blitted into the GUI.
- Restored schema light preparation through the target `LevelLightEngine`, made the filtered schema level implement `BlockAndTintGetter`, and migrated item quad extraction and camera projection helpers away from empty/identity fallbacks.
- Added Java 25 `jdk.compiler` opens and module-local delombok processing for the Lombok-heavy upstream source.

## Verification

- `JAVA_HOME=C:\\Program Files\\Java\\jdk-25.0.2 .\\gradlew.bat --no-daemon :modularui:compileJava` passes.
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-25.0.2 .\\gradlew.bat --no-daemon :modularui:test` passes: 10 tests, 0 failures.
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-25.0.2 .\\gradlew.bat --no-daemon :modularui:jar` passes.
- `JAVA_HOME=C:\\Program Files\\Java\\jdk-25.0.2 .\\gradlew.bat --no-daemon :modularui:check` passes.
- `git diff --check` passes.

## Remaining Boundary

- The root `:compileJava` check reaches the module successfully but remains blocked by unrelated GTM task-10+ migration errors outside `:modularui`, including legacy recipe serializers, Forge capability types, tool APIs, and other target API changes.
- Minecraft `26.1` moved GUI rendering to render-state extraction and removed the old world chunk upload/shader stack. The schema drawable retains its public camera/filter/widget API and now routes its 3D scene through the target Picture-in-Picture render-state contract.
- The target has no client `RecipeManager` object; client-side code paths using the old manager must use `RecipeAccess` or remain server-owned.
- Clean client and server runtime smoke checks still require the broader GTM task-10+ build to be green before they can be trusted.
