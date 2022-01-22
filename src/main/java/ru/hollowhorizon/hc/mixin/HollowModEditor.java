package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.fml.javafmlmod.FMLModContainer;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.registy.HollowMod;
import ru.hollowhorizon.hc.common.registry.HollowModProcessor;

@Mixin(value = FMLModContainer.class)
public abstract class HollowModEditor {
    @Shadow(remap = false)
    @Final
    private ModFileScanData scanResults;
    @Shadow(remap = false)
    @Final
    private Class<?> modClass;

    @Inject(method = "constructMod",
            at = @At(value = "TAIL"),
            remap = false
    )
    public void fmlModConstructingHook(CallbackInfo ci) {
        FMLModContainer modContainer = (FMLModContainer) (Object) this;
        Object mod = getMod();
        String modId = modContainer.getModId();
        if (mod instanceof HollowMod) {
            HollowModProcessor.run(modId, scanResults);
        }
    }

    @Shadow(remap = false)
    public abstract Object getMod();

}
