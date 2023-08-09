package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2;

@Mixin(value = CapabilityManager.class, remap = false)
public class CapabilityManagerMixin {

    private CapabilityManagerMixin() {
    }

    @Shadow(remap = false)
    @Final
    private static Type AUTO_REGISTER;

    private static final Type HOLLOW_CAP = Type.getType(HollowCapabilityV2.class);

    @Inject(method = "lambda$injectCapabilities$1", at = @At(value = "INVOKE", target = "Lorg/objectweb/asm/Type;equals(Ljava/lang/Object;)Z"), cancellable = true, remap = false)
    private static void injectCapabilities(ModFileScanData.AnnotationData a, CallbackInfoReturnable<Boolean> cir) {

        if(HOLLOW_CAP.equals(a.annotationType())) HollowCore.LOGGER.info("HollowCapability found: {}", a.memberName());
        cir.setReturnValue(AUTO_REGISTER.equals(a.annotationType()) || HOLLOW_CAP.equals(a.annotationType()));
    }


}
