package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicate;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.NBTPredicates;
import com.gregtechceu.gtceu.api.recipe.ingredient.nbtpredicate.TrueNBTPredicate;
import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.stream.Stream;

public final class NBTPredicateIngredient implements ICustomIngredient {

    public static final MapCodec<NBTPredicateIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("stack").forGetter(value -> value.stack),
            net.minecraft.util.ExtraCodecs.JSON.fieldOf("predicate")
                    .xmap(value -> NBTPredicates.fromJson(value.getAsJsonObject()), NBTPredicate::toJson)
                    .forGetter(value -> value.predicate))
            .apply(instance, NBTPredicateIngredient::new));

    public static final NBTPredicate ALWAYS_TRUE = new TrueNBTPredicate();
    private final NBTPredicate predicate;
    private final ItemStack stack;

    public NBTPredicateIngredient(ItemStack stack, NBTPredicate predicate) {
        this.stack = stack.copy();
        this.predicate = predicate;
    }

    public static Ingredient of(ItemStack stack) {
        return of(stack, ALWAYS_TRUE);
    }

    public static Ingredient of(ItemStack stack, NBTPredicate predicate) {
        return new NBTPredicateIngredient(stack, predicate).toVanilla();
    }

    @Override
    public boolean test(ItemStack input) {
        CustomData customData = input.get(DataComponents.CUSTOM_DATA);
        return !input.isEmpty() && input.is(stack.getItem())
                && predicate.test(customData == null ? new net.minecraft.nbt.CompoundTag() : customData.copyTag());
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(stack.getItem().builtInRegistryHolder());
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.NBT_PREDICATE.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof NBTPredicateIngredient other && stack.equals(other.stack)
                && predicate.equals(other.predicate);
    }

    @Override
    public int hashCode() {
        return 31 * stack.hashCode() + predicate.hashCode();
    }
}
