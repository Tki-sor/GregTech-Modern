package com.gregtechceu.gtceu.data.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntCircuitIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.IntProviderIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.NBTPredicateIngredient;
import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class GTIngredientTypes {

    public static final DeferredRegister<IngredientType<?>> ITEM_INGREDIENT_TYPES = DeferredRegister.create(
            NeoForgeRegistries.INGREDIENT_TYPES, GTCEu.MOD_ID);

    public static final DeferredHolder<IngredientType<?>, IngredientType<SizedIngredient>> SIZED = ITEM_INGREDIENT_TYPES
            .register("sized", () -> new IngredientType<>(SizedIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<IntProviderIngredient>> INT_PROVIDER = ITEM_INGREDIENT_TYPES
            .register("int_provider", () -> new IngredientType<>(IntProviderIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<IntCircuitIngredient>> CIRCUIT = ITEM_INGREDIENT_TYPES
            .register("circuit", () -> new IngredientType<>(IntCircuitIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<NBTPredicateIngredient>> NBT_PREDICATE = ITEM_INGREDIENT_TYPES
            .register("nbt_predicate", () -> new IngredientType<>(NBTPredicateIngredient.CODEC));
    public static final DeferredHolder<IngredientType<?>, IngredientType<FluidContainerIngredient>> FLUID_CONTAINER = ITEM_INGREDIENT_TYPES
            .register("fluid_container", () -> new IngredientType<>(FluidContainerIngredient.CODEC));

    private GTIngredientTypes() {}

    public static void init(IEventBus modBus) {
        ITEM_INGREDIENT_TYPES.register(modBus);
    }
}
