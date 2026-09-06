package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.behavior.IntCircuitBehaviour;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public final class IntCircuitIngredient implements ICustomIngredient {

    public static final MapCodec<IntCircuitIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            com.mojang.serialization.Codec.intRange(0, 32).fieldOf("configuration")
                    .forGetter(IntCircuitIngredient::getConfiguration))
            .apply(instance, IntCircuitIngredient::new));

    private static final IntCircuitIngredient[] INGREDIENTS = new IntCircuitIngredient[33];
    private final int configuration;
    private final ItemStack stack;

    private IntCircuitIngredient(int configuration) {
        this.configuration = configuration;
        this.stack = IntCircuitBehaviour.stack(configuration);
    }

    public static Ingredient of(int configuration) {
        if (configuration < 0 || configuration > 32) {
            throw new IndexOutOfBoundsException("Circuit configuration " + configuration + " is out of range");
        }
        IntCircuitIngredient value = INGREDIENTS[configuration];
        if (value == null) INGREDIENTS[configuration] = value = new IntCircuitIngredient(configuration);
        return value.toVanilla();
    }

    public int getConfiguration() {
        return configuration;
    }

    public Ingredient convertToData() {
        return DataComponentIngredient.of(true, stack);
    }

    @Override
    public boolean test(ItemStack input) {
        return !input.isEmpty() && input.is(GTItems.PROGRAMMED_CIRCUIT.get())
                && IntCircuitBehaviour.getCircuitConfiguration(input) == configuration;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(GTItems.PROGRAMMED_CIRCUIT.get().builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.CIRCUIT.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IntCircuitIngredient other && configuration == other.configuration;
    }

    @Override
    public int hashCode() {
        return configuration;
    }
}
