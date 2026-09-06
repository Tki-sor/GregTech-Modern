package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IElectricItem;
import com.gregtechceu.gtceu.utils.IngredientUtils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
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

public final class ShapedEnergyTransferRecipe extends ShapedRecipe {

    public static final MapCodec<ShapedEnergyTransferRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(recipe -> recipe.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(recipe -> recipe.bookInfo),
            ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(recipe -> recipe.resultTemplate),
            Ingredient.CODEC.fieldOf("chargeIngredient").forGetter(recipe -> recipe.chargeIngredient),
            ByteBufCodecs.BOOL.codec().fieldOf("overrideCharge").forGetter(recipe -> recipe.overrideCharge),
            ByteBufCodecs.BOOL.codec().fieldOf("transferMaxCharge").forGetter(recipe -> recipe.transferMaxCharge))
            .apply(instance, ShapedEnergyTransferRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedEnergyTransferRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, recipe -> recipe.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, recipe -> recipe.bookInfo,
            ShapedRecipePattern.STREAM_CODEC, recipe -> recipe.pattern,
            ItemStackTemplate.STREAM_CODEC, recipe -> recipe.resultTemplate,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.chargeIngredient,
            ByteBufCodecs.BOOL, recipe -> recipe.overrideCharge,
            ByteBufCodecs.BOOL, recipe -> recipe.transferMaxCharge,
            ShapedEnergyTransferRecipe::new);

    public static final RecipeSerializer<ShapedEnergyTransferRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    private final ItemStackTemplate resultTemplate;
    private final Ingredient chargeIngredient;
    private final boolean transferMaxCharge;
    private final boolean overrideCharge;

    public ShapedEnergyTransferRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, ItemStackTemplate resultTemplate,
                                      Ingredient chargeIngredient, boolean overrideCharge, boolean transferMaxCharge) {
        super(commonInfo, bookInfo, pattern, resultTemplate);
        this.resultTemplate = resultTemplate;
        this.chargeIngredient = chargeIngredient;
        this.transferMaxCharge = transferMaxCharge;
        this.overrideCharge = overrideCharge;
    }

    public Ingredient getChargeIngredient() { return chargeIngredient; }
    public boolean isTransferMaxCharge() { return transferMaxCharge; }
    public boolean isOverrideCharge() { return overrideCharge; }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack result = super.assemble(input);
        long maxCharge = 0;
        long charge = 0;
        for (ItemStack candidate : IngredientUtils.getItems(chargeIngredient)) {
            for (int i = 0; i < input.size(); i++) {
                ItemStack stack = input.getItem(i);
                if (ItemStack.isSameItem(stack, candidate)) {
                    IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
                    if (electricItem != null) {
                        maxCharge += electricItem.getMaxCharge();
                        charge += electricItem.getCharge();
                    }
                }
            }
        }
        CompoundTag data = new CompoundTag();
        data.putLong("MaxCharge", maxCharge);
        data.putLong("Charge", charge);
        CustomData.set(DataComponents.CUSTOM_DATA, result, data);
        return result;
    }

    @Override
    public RecipeSerializer<ShapedEnergyTransferRecipe> getSerializer() {
        return SERIALIZER;
    }
}
