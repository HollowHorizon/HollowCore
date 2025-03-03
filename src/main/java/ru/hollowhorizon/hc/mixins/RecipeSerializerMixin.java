package ru.hollowhorizon.hc.mixins;

import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowRecipeSerializer;

@Mixin(RecipeSerializer.class)
interface RecipeSerializerMixin<T extends Recipe<?>> extends HollowRecipeSerializer<T> {}
