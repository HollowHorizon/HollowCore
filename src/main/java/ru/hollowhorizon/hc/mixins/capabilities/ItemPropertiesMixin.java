//? if >=1.21 {
/*package ru.hollowhorizon.hc.mixins.capabilities;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.Properties.class)
public abstract class ItemPropertiesMixin {
    @Shadow public abstract <T> Item.Properties component(DataComponentType<T> component, T value);

    @Inject(method="<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        //component(CapabilityComponents.INSTANCE.getCAPABILITIES().get(), new DefaultDispatcher());
    }
}
*///?}