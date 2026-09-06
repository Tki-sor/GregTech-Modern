package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntObjectPair;

import java.util.Optional;

public final class ShapedFluidContainerRecipe extends ShapedRecipe {

    public static final MapCodec<ShapedFluidContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate))
            .apply(instance, ShapedFluidContainerRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
            ShapedFluidContainerRecipe::new);

    public static final RecipeSerializer<ShapedFluidContainerRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final ItemStackTemplate resultTemplate;

    public ShapedFluidContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, ItemStackTemplate resultTemplate) {
        super(commonInfo, bookInfo, pattern, resultTemplate);
        this.resultTemplate = resultTemplate;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> items = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        int replacedSlot = -1;
        for (int x = 0; x <= input.width() - getWidth() && replacedSlot < 0; ++x) {
            for (int y = 0; y <= input.height() - getHeight(); ++y) {
                IntObjectPair<ItemStack> replacement = findFluidReplacement(input, x, y, false);
                if (replacement.firstInt() < 0) replacement = findFluidReplacement(input, x, y, true);
                if (replacement.firstInt() >= 0) {
                    items.set(replacement.firstInt(), replacement.second());
                    replacedSlot = replacement.firstInt();
                    break;
                }
            }
        }
        for (int i = 0; i < items.size(); i++) {
            if (i == replacedSlot) continue;
            ItemStack item = input.getItem(i);
            ItemStack remainder = item.getCraftingRemainder() == null ? ItemStack.EMPTY : item.getCraftingRemainder().create();
            items.set(i, remainder);
        }
        return items;
    }

    private IntObjectPair<ItemStack> findFluidReplacement(CraftingInput input, int width, int height, boolean mirrored) {
        for (int x = 0; x < input.width(); x++) {
            for (int y = 0; y < input.height(); y++) {
                int offsetX = x - width;
                int offsetY = y - height;
                Optional<Ingredient> optional = Optional.empty();
                if (offsetX >= 0 && offsetY >= 0 && offsetX < getWidth() && offsetY < getHeight()) {
                    int index = (mirrored ? getWidth() - offsetX - 1 : offsetX) + offsetY * getWidth();
                    optional = pattern.ingredients().get(index);
                }
                if (optional.isPresent() && optional.get().isCustom()
                        && optional.get().getCustomIngredient() instanceof FluidContainerIngredient fluid) {
                    int slot = x + y * input.width();
                    ItemStack stack = input.getItem(slot);
                    if (fluid.test(stack)) return IntObjectPair.of(slot, fluid.getExtractedStack(stack));
                }
            }
        }
        return IntObjectPair.of(-1, ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<ShapedFluidContainerRecipe> getSerializer() {
        return SERIALIZER;
    }
}
