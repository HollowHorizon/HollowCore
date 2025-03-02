package ru.hollowhorizon.hc.mixins;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.objects.recipe.HollowCoreIngredient;
import ru.hollowhorizon.hc.common.objects.recipe.HollowIngredient;
import ru.hollowhorizon.hc.common.objects.recipe.HollowRecipeHelper;

@Mixin(Ingredient.class)
public class IngredientMixin implements HollowCoreIngredient {
    @Inject(
            method = "fromJson(Lcom/google/gson/JsonElement;Z)Lnet/minecraft/world/item/crafting/Ingredient;",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/crafting/Ingredient;valueFromJson(Lcom/google/gson/JsonObject;)Lnet/minecraft/world/item/crafting/Ingredient$Value;",
                    ordinal = 0
            ),
            cancellable = true
    )
    private static void fromJson(JsonElement json, boolean canBeEmpty, CallbackInfoReturnable<Ingredient> cir) {
        var obj = json.getAsJsonObject();

        if (!obj.has(HollowIngredient.Companion.getTypeKey())) return;
        var id = ForgeKotlinKt.getRl(GsonHelper.getAsString(obj, HollowIngredient.Companion.getTypeKey()));
        var serializer = HollowIngredient.getSerializer(id);

        if (serializer == null) throw new IllegalArgumentException("Unknown ingredient type: " + id);
        cir.setReturnValue(serializer.fromJson(obj).getAsVanilla());
    }

    @Inject(
            method = "valueFromJson",
            at = @At("HEAD")
    )
    private static void valueFromJson(JsonObject json, CallbackInfoReturnable<Ingredient.Value> cir) {
        if (json.has(HollowIngredient.Companion.getTypeKey())) {
            throw new IllegalArgumentException("Ingredient cannot be used inside an array ingredient. You can replace the array by a `any` ingredient.");
        }
    }

    @Inject(
            method = "fromNetwork",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void fromNetwork(FriendlyByteBuf buffer, CallbackInfoReturnable<Ingredient> cir) {
        var ix = buffer.readerIndex();

        if (buffer.readVarInt() != HollowIngredient.Companion.getPacketMarker()) {
            buffer.readerIndex(ix);
            return;
        }

        var id = buffer.readResourceLocation();
        var serializer = HollowRecipeHelper.getIngredientSerializer(id);

        if (serializer == null) {
            throw new IllegalArgumentException("Cannot deserialize ingredient of unknown type " + id);
        }

        cir.setReturnValue(serializer.fromNetwork(buffer).getAsVanilla());
    }
}
