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
        cir.setReturnValue(HollowRecipeHelper.getItemStack(stackObject, true, true));
    }
}
