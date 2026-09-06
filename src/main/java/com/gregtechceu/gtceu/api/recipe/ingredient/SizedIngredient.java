package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;
import com.gregtechceu.gtceu.utils.IngredientUtils;

import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;
import java.util.stream.Stream;

/** An item ingredient which retains the required stack count in recipe data. */
public final class SizedIngredient implements ICustomIngredient {

    public static final net.minecraft.resources.Identifier TYPE = com.gregtechceu.gtceu.GTCEu.id("sized");

    public static final MapCodec<SizedIngredient> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(SizedIngredient::getInner),
            com.mojang.serialization.Codec.intRange(1, Integer.MAX_VALUE).fieldOf("count")
                    .forGetter(SizedIngredient::getAmount))
            .apply(instance, SizedIngredient::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SizedIngredient> STREAM_CODEC = StreamCodec.composite(
            IngredientUtils.STREAM_CODEC, SizedIngredient::getInner,
            ByteBufCodecs.VAR_INT, SizedIngredient::getAmount,
            SizedIngredient::new);

    private final Ingredient inner;
    private final int amount;

    public SizedIngredient(Ingredient inner, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Ingredient count must be positive");
        this.inner = inner;
        this.amount = amount;
    }

    public static Ingredient create(ItemStack stack) {
        return create(ingredientFor(stack), stack.getCount());
    }

    public static Ingredient create(Ingredient inner) {
        return create(inner, 1);
    }

    public static Ingredient create(Ingredient inner, int amount) {
        return new SizedIngredient(inner, amount).toVanilla();
    }

    public static Ingredient create(net.minecraft.tags.TagKey<Item> tag, int amount) {
        return create(Ingredient.of(tag), amount);
    }

    private static Ingredient ingredientFor(ItemStack stack) {
        return stack.isComponentsPatchEmpty() ? Ingredient.of(stack.getItem())
                : net.neoforged.neoforge.common.crafting.DataComponentIngredient.of(true, stack);
    }

    public static Ingredient copy(Ingredient ingredient) {
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof SizedIngredient sized) {
            return create(sized.inner, sized.amount);
        }
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof IntProviderIngredient provider) {
            return provider.copy().toVanilla();
        }
        ItemStack[] items = IngredientUtils.getItems(ingredient);
        return items.length == 0 ? Ingredient.EMPTY : create(ingredient, items[0].getCount());
    }

    public static SizedIngredient get(Ingredient ingredient) {
        return ingredient.isCustom() && ingredient.getCustomIngredient() instanceof SizedIngredient sized ? sized : null;
    }

    public static Ingredient getInner(Ingredient ingredient) {
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof SizedIngredient sized) {
            return getInner(sized.inner);
        }
        if (ingredient.isCustom() && ingredient.getCustomIngredient() instanceof IntProviderIngredient provider) {
            return getInner(provider.getInner());
        }
        return ingredient;
    }

    public Ingredient getInner() {
        return inner;
    }

    public int getAmount() {
        return amount;
    }

    public ItemStack[] getItems() {
        return IngredientUtils.getItems(inner);
    }

    @Override
    public boolean test(ItemStack stack) {
        return inner.test(stack) && stack.getCount() >= amount;
    }

    @Override
    public Stream<Holder<Item>> items() {
        return inner.items();
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public IngredientType<?> getType() {
        return GTIngredientTypes.SIZED.get();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof SizedIngredient other && amount == other.amount && inner.equals(other.inner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(inner, amount);
    }
}
