package ru.hollowhorizon.hc.common.objects.recipe.builtin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.common.objects.recipe.HollowRecipeHelper;
import ru.hollowhorizon.hc.common.objects.recipe.condition.HollowCondition;
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowRecipeSerializer;
import ru.hollowhorizon.hc.common.utils.JavaHacks;

import java.util.Objects;

public class ConditionalRecipeSerializer<T extends Recipe<?>> implements RecipeSerializer<T>, HollowRecipeSerializer<T> {
    @Override
    public @Nullable T fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json, @NotNull HollowCondition.ConditionContext ctx) {
        JsonArray items = GsonHelper.getAsJsonArray(json, "recipes");
        int idx = 0;
        for (JsonElement ele : items)
        {
            if (!ele.isJsonObject())
                throw new JsonSyntaxException("Invalid recipes entry at index " + idx + " Must be JsonObject");
            if (HollowRecipeHelper.processConditions(GsonHelper.getAsJsonArray(ele.getAsJsonObject(), "conditions"), ctx))
                return JavaHacks.forceCast(RecipeManager.fromJson(id, GsonHelper.getAsJsonObject(ele.getAsJsonObject(), "recipe")));
            idx++;
        }
        return null;
    }

    @Override
    public T fromJson(@NotNull ResourceLocation recipeId, @NotNull JsonObject serializedRecipe) {
        return Objects.requireNonNull(fromJson(recipeId, serializedRecipe, HollowCondition.ConditionContext.EMPTY));
    }

    @Override
    public T fromNetwork(@NotNull ResourceLocation recipeId, @NotNull FriendlyByteBuf buffer) {
        return null;
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, T recipe) {}
}
