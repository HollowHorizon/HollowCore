package ru.hollowhorizon.hc.api.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nullable;

public class HollowRecipeSerializer<T extends HollowRecipe> extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<T> {

    public final HollowFactory<T> factory;

    public HollowRecipeSerializer(HollowFactory<T> factory) {
        this.factory = factory;
    }

    @Override
    public T fromJson(ResourceLocation recipeId, JsonObject json) {
        String s = JSONUtils.getAsString(json, "group", "");
        JsonElement jsonelement = JSONUtils.isArrayNode(json, "ingredient") ? JSONUtils.getAsJsonArray(json, "ingredient") : JSONUtils.getAsJsonObject(json, "ingredient");
        Ingredient ingredient = Ingredient.fromJson(jsonelement);

        if (!json.has("result"))
            throw new com.google.gson.JsonSyntaxException("Missing result, expected to find a string or object");
        ItemStack itemstack;
        if (json.get("result").isJsonObject())
            itemstack = ShapedRecipe.itemFromJson(JSONUtils.getAsJsonObject(json, "result"));
        else {
            String s1 = JSONUtils.getAsString(json, "result");
            ResourceLocation resourcelocation = new ResourceLocation(s1);
            itemstack = new ItemStack(ForgeRegistries.ITEMS.getValue(resourcelocation));
        }
        float f = JSONUtils.getAsFloat(json, "experience", 0.0F);
        int i = JSONUtils.getAsInt(json, "cookingtime", 0);
        return this.factory.create(recipeId, s, ingredient, itemstack, f, i);
    }

    @Nullable
    @Override
    public T fromNetwork(ResourceLocation recipeId, PacketBuffer buffer) {
        String s = buffer.readUtf(32767);
        Ingredient ingredient = Ingredient.fromNetwork(buffer);
        ItemStack itemstack = buffer.readItem();
        float f = buffer.readFloat();
        int i = buffer.readVarInt();
        return this.factory.create(recipeId, s, ingredient, itemstack, f, i);
    }

    @Override
    public void toNetwork(PacketBuffer buffer, T recipe) {
        buffer.writeUtf(recipe.group);
        recipe.ingredient.toNetwork(buffer);
        buffer.writeItem(recipe.result);
        buffer.writeFloat(recipe.experience);
        buffer.writeVarInt(recipe.time);
    }

    public  interface HollowFactory<T extends HollowRecipe> {
            T create(ResourceLocation p_create_1_, String p_create_2_, Ingredient p_create_3_, ItemStack p_create_4_, float p_create_5_, int p_create_6_);
    }
}
