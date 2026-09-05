package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredient;
import com.gregtechceu.gtceu.utils.IngredientUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;

public class SerializerIngredient implements IContentSerializer<Ingredient> {

    public static final Codec<Ingredient> CODEC = Ingredient.CODEC;

    public static SerializerIngredient INSTANCE = new SerializerIngredient();

    private SerializerIngredient() {}

    @Override
    public void toNetwork(FriendlyByteBuf buf, Ingredient content) {
        IngredientUtils.STREAM_CODEC.encode((net.minecraft.network.RegistryFriendlyByteBuf) buf, content);
    }

    @Override
    public Ingredient fromNetwork(FriendlyByteBuf buf) {
        return IngredientUtils.STREAM_CODEC.decode((net.minecraft.network.RegistryFriendlyByteBuf) buf);
    }

    @Override
    public Ingredient fromJson(JsonElement json) {
        return IngredientUtils.fromJson(json);
    }

    @Override
    public JsonElement toJson(Ingredient content) {
        return IngredientUtils.toJson(content);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Ingredient of(Object o) {
        if (o instanceof Ingredient ingredient) {
            return ingredient;
        } else if (o instanceof ItemStack itemStack) {
            return SizedIngredient.create(itemStack);
        } else if (o instanceof ItemLike itemLike) {
            return Ingredient.of(itemLike);
        } else if (o instanceof TagKey tag) {
            return Ingredient.of(tag);
        }
        return Ingredient.EMPTY;
    }

    @Override
    public Ingredient defaultValue() {
        return Ingredient.EMPTY;
    }

    @Override
    public Class<Ingredient> contentClass() {
        return Ingredient.class;
    }

    @Override
    public Codec<Ingredient> codec() {
        return CODEC;
    }
}
