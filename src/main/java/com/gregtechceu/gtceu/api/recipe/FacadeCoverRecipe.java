package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.item.behavior.FacadeItemBehaviour;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;

public final class FacadeCoverRecipe implements CraftingRecipe {

    public static final FacadeCoverRecipe INSTANCE = new FacadeCoverRecipe();
    public static final MapCodec<FacadeCoverRecipe> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeCoverRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);
    public static final RecipeSerializer<FacadeCoverRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    public static final net.minecraft.resources.Identifier ID = GTCEu.id("crafting/facade_cover");

    private FacadeCoverRecipe() {}

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int platesCount = 0;
        boolean foundBlockItem = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack item = input.getItem(i);
            if (item.isEmpty()) continue;
            if (FacadeItemBehaviour.isValidFacade(item)) {
                if (foundBlockItem) return false;
                foundBlockItem = true;
            } else if (item.is(ChemicalHelper.getTagOrThrow(TagPrefix.plate, GTMaterials.Iron))) {
                if (platesCount > 1) return false;
                platesCount++;
            } else return false;
        }
        return foundBlockItem && platesCount == 1;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = GTItems.COVER_FACADE.asStack(6);
        BlockState facadeState = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack item = input.getItem(i);
            if (!item.isEmpty() && FacadeItemBehaviour.isValidFacade(item)) {
                facadeState = FacadeItemBehaviour.getFacadeState(item);
                break;
            }
        }
        if (facadeState == null) return ItemStack.EMPTY;
        FacadeItemBehaviour.setFacadeState(result, facadeState);
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY,
                Ingredient.of(ChemicalHelper.getTagOrThrow(TagPrefix.plate, GTMaterials.Iron)),
                Ingredient.of(Blocks.STONE));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    public ItemStack getResultItem() {
        ItemStack result = GTItems.COVER_FACADE.asStack(6);
        FacadeItemBehaviour.setFacadeState(result, Blocks.STONE.defaultBlockState());
        return result;
    }

    @Override
    public net.minecraft.resources.Identifier getId() { return ID; }

    @Override
    public RecipeSerializer<?> getSerializer() { return SERIALIZER; }

    @Override
    public CraftingBookCategory category() { return CraftingBookCategory.MISC; }

    @Override
    public PlacementInfo placementInfo() { return PlacementInfo.create(getIngredients()); }
}
