# Task 10 Core NeoForge API

## Implemented

- Replaced the Forge mod entrypoint with NeoForge constructor injection using the mod event bus and `FMLModContainer` boundary.
- Added a shared GTM mod bus reference, common initialization path, NeoForge client-side initialization path, data-generation detection, mod-loaded checks, and NeoForge server lifecycle lookup.
- Moved common and game event subscribers to NeoForge packages and event shapes, including player, level, command, reload, server, and client-side event registration.
- Replaced `ResourceLocation` and resource-location buffer helpers with 26.1 `Identifier` equivalents across main and direct API-import test sources.
- Added NeoForge custom registry backing registries built from `RegistryBuilder`, registered through `NewRegistryEvent` and `RegisterEvent`, with holder lookup helpers and frozen registry access tracking.
- Migrated deferred registration boundaries used by core menu, command argument, particle, mob effect, and related registration classes to `DeferredRegister` and `DeferredHolder`.
- Replaced Forge capability tokens with named NeoForge `BlockCapability`, `ItemCapability`, and `EntityCapability` keys. Public helpers now use NeoForge direct capability lookup and legacy handler adapters where the transfer rewrite still owns semantics.
- Registered the GTM medical tracker as a NeoForge player entity capability while retaining the GTM domain tracker type.
- Removed the obsolete Forge attach-capability event handlers and Forge missing-mapping event dependency from the core listener. Missing mapping aliases require the target deferred/registry migration owned by later data and integration work.
- Applied the mechanical Forge-to-NeoForge package migration to the remaining Java source, while leaving behavior rewrites for later tickets.

## Remaining Diagnostics

The final incremental `compileJava` reaches Java compilation under Java 25 and reports the following expected later-ticket groups:

- Task 11: `Ingredient`/`RecipeSerializer`/finished-recipe API changes, removed `MethodsReturnNonnullByDefault`, datapack and datagen types, and data-driven registry/codec migrations.
- Task 12: old `Capability`/`ForgeCapabilities`/`LazyOptional`/`IForge*` transfer boundaries, item/fluid/energy handler semantics, and capability providers in machines, pipes, and covers.
- Task 13: old packet classes and `SimpleChannel`/`NetworkEvent`/`PacketDistributor` implementation, which must be rewritten to payload types and `StreamCodec`.
- Task 14: 26.1 client rendering changes including `BakedModel`, `BlockAndTintGetter`, `ModelData`, renderer state generics, model generators, and mixin/render targets.
- Task 15: optional API changes in KubeJS, AE2, Jade, Create, recipe viewers, and remaining integration-specific registry access.

The initial compile attempt also encountered TLS handshake failures for the pinned third-party Maven hosts. After the required artifacts became available in the local Gradle cache, compilation progressed to the source diagnostics above.
