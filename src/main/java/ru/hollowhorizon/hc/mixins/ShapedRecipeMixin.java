package ru.hollowhorizon.hc.mixins;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.common.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.utils.HollowRecipeHelper;

@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
    @Inject(method = "itemStackFromJson", at = @At("HEAD"), cancellable = true)
    private static void itemStackFromJson(JsonObject stackObject, CallbackInfoReturnable<ItemStack> cir) {
        var typeKey = "hollowcore:type";
        if (!stackObject.has(typeKey)) return;
        var id = ForgeKotlinKt.getRl(GsonHelper.getAsString(stackObject, typeKey));
        if (id != ForgeKotlinKt.getRl("hollowcore:expanded")) return; // A safe option to avoid accidentally breaking the entire method

        cir.setReturnValue(HollowRecipeHelper.getItemStack(stackObject, true, false));
    }
}
