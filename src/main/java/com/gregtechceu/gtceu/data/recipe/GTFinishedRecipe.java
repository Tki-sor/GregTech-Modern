package com.gregtechceu.gtceu.data.recipe;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

/** Internal recipe-generation record used by GTM's runtime and dynamic pack writers. */
public interface GTFinishedRecipe {
    void serializeRecipeData(JsonObject json);

    Identifier getId();

    RecipeSerializer<?> getType();

    @Nullable
    JsonObject serializeAdvancement();

    @Nullable
    Identifier getAdvancementId();

    default JsonObject serializeRecipe() {
        JsonObject json = new JsonObject();
        json.addProperty("type", getTypeId().toString());
        serializeRecipeData(json);
        return json;
    }

    default Identifier getTypeId() {
        return net.minecraft.core.registries.BuiltInRegistries.RECIPE_SERIALIZER.getKey(getType());
    }
}
