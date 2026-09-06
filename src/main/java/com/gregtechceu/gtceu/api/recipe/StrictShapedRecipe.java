package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
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

public final class StrictShapedRecipe extends ShapedRecipe {

    public static final MapCodec<StrictShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate),
            com.mojang.serialization.Codec.BOOL.optionalFieldOf("match_size", false).forGetter(recipe -> recipe.matchSize))
            .apply(instance, StrictShapedRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
            ByteBufCodecs.BOOL, recipe -> recipe.matchSize,
            StrictShapedRecipe::new);

    public static final RecipeSerializer<StrictShapedRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final ItemStackTemplate resultTemplate;
    private final boolean matchSize;

    public StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                              ShapedRecipePattern pattern, ItemStackTemplate resultTemplate, boolean matchSize) {
        super(commonInfo, bookInfo, pattern, resultTemplate);
        this.resultTemplate = resultTemplate;
        this.matchSize = matchSize;
    }

    public boolean matchSize() {
        return matchSize;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (matchSize && (input.width() != getWidth() || input.height() != getHeight())) return false;
        return pattern.matches(input);
    }

    @Override
    public RecipeSerializer<StrictShapedRecipe> getSerializer() {
        return SERIALIZER;
    }
}
