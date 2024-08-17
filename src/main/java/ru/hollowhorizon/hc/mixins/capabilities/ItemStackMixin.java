package ru.hollowhorizon.hc.mixins.capabilities;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;


//
//import net.minecraft.core.component.DataComponentMap;
//import net.minecraft.core.component.PatchedDataComponentMap;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.level.ItemLike;
//import org.jetbrains.annotations.NotNull;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Inject;
//import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
//import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
//import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
//
//import java.util.List;
//
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {//implements ICapabilityDispatcher {
//    @Shadow
//    public abstract DataComponentMap getComponents();
//
//    @Inject(
//            method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
//            at = @At("TAIL")
//    )
//    private void onInit(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
//        ICapabilityDispatcherKt.initialize(this);
//    }
//
//    @NotNull
//    @Override
//    public List<CapabilityInstance> getCapabilities() {
//        return getComponents()
//                // .getOrDefault(CapabilityComponents.INSTANCE.getCAPABILITIES().get(), new DefaultDispatcher())
//                .getCapabilities();
//    }
}
