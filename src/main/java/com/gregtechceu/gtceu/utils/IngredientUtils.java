package com.gregtechceu.gtceu.utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.IngredientCodecs;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import java.util.stream.Stream;

public final class IngredientUtils {

    public static final StreamCodec<RegistryFriendlyByteBuf, Ingredient> STREAM_CODEC = IngredientCodecs.streamCodec(
            Ingredient.CONTENTS_STREAM_CODEC);

    private IngredientUtils() {}

    public static Ingredient fromJson(JsonElement json) {
        return Ingredient.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, message -> {
            throw new IllegalArgumentException("Invalid ingredient: " + message);
        });
    }

    public static JsonElement toJson(Ingredient ingredient) {
        return Ingredient.CODEC.encodeStart(JsonOps.INSTANCE, ingredient).getOrThrow(false, message -> {
            throw new IllegalStateException("Could not encode ingredient: " + message);
        });
    }

    public static ItemStack[] getItems(Ingredient ingredient) {
        return ingredient.items().map(holder -> holder.value().getDefaultInstance()).toArray(ItemStack[]::new);
    }

    public static Stream<ItemStack> itemStream(Ingredient ingredient) {
        return ingredient.items().map(holder -> holder.value().getDefaultInstance());
    }
}
