package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow()
    public abstract Item getItem();


    @Shadow()
    public abstract CompoundNBT getOrCreateTag();

    @Inject(method = "hasFoil", at = @At(value = "TAIL"), cancellable = true)
    private void haveFoil(CallbackInfoReturnable<Boolean> cir) {
        if (this.getOrCreateTag().contains("use_glint")) {
                cir.setReturnValue(true);
                return;
        }

        cir.setReturnValue(cir.getReturnValueZ());
    }
}


