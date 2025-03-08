package ru.hollowhorizon.hc.mixins;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.objects.recipe.deep.HollowCoreIngredient;
import ru.hollowhorizon.hc.common.objects.recipe.ShapelessMatch;

import java.util.ArrayList;

@Mixin(ShapelessRecipe.class)
public class ShapelessRecipeMixin {
    @Shadow @Final
    NonNullList<Ingredient> ingredients;
    @Unique
    private boolean hc$reqTest = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(ResourceLocation id, String group, CraftingBookCategory category, ItemStack result, NonNullList<Ingredient> ingredients, CallbackInfo ci) {
        for (Ingredient ingredient : this.ingredients) {
            if (((HollowCoreIngredient) ingredient).getRequireTesting()) {
                hc$reqTest = true;
                break;
            }
        }
    }

    @Inject(
            method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void matches(CraftingContainer inv, Level level, CallbackInfoReturnable<Boolean> cir) {
        if (!hc$reqTest) return;
        var nns = new ArrayList<ItemStack>(inv.getContainerSize());

        for (int i = 0; i < inv.getContainerSize(); i++) {
            var stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            nns.add(stack);
        }

        cir.setReturnValue(ShapelessMatch.matches(nns, this.ingredients));
    }
}
