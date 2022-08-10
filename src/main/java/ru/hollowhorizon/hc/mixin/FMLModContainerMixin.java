package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.api.registy.HollowMod;
import ru.hollowhorizon.hc.common.registry.HollowModProcessor;

@Mixin(value = FMLModContainer.class)
public abstract class FMLModContainerMixin {
    @Shadow(remap = false)
    @Final
    private Class<?> modClass;
    @Shadow(remap = false)
    @Final
    private ModFileScanData scanResults;

    @Inject(method = "constructMod",
            at = @At(value = "TAIL"),
            remap = false
    )
    public void fmlModConstructingHook(CallbackInfo ci) {
        FMLModContainer modContainer = (FMLModContainer) (Object) this;
        String modId = modContainer.getModId();
        if (modClass.isAnnotationPresent(HollowMod.class)) {
            HollowModProcessor.run(modId, scanResults);
        }
    }
}