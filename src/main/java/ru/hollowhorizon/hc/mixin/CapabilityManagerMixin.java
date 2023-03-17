package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2;
import ru.hollowhorizon.hc.common.capabilities.provider.CapabilityBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Mixin(value = CapabilityManager.class, remap = false)
public class CapabilityManagerMixin {

    private CapabilityManagerMixin() {
    }

    @Shadow(remap = false)
    @Final
    private static Type CAP_INJECT;

    private static final Type HOLLOW_CAP = Type.getType(HollowCapabilityV2.class);

    @Inject(method = "lambda$injectCapabilities$1", at = @At(value = "INVOKE", target = "Lorg/objectweb/asm/Type;equals(Ljava/lang/Object;)Z"), cancellable = true, remap = false)
    private static void injectCapabilities(ModFileScanData.AnnotationData a, CallbackInfoReturnable<Boolean> cir) {
        if(HOLLOW_CAP.equals(a.getAnnotationType())) HollowCore.LOGGER.info("HollowCapability found: {}", a.getMemberName());
        cir.setReturnValue(CAP_INJECT.equals(a.getAnnotationType()) || HOLLOW_CAP.equals(a.getAnnotationType()));
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "attachCapabilityToMethod", at = @At("HEAD"), cancellable = true, remap = false)
    private static void attachCapabilityToMethod(Map<String, List<Function<Capability<?>, Object>>> cbs, ModFileScanData.AnnotationData entry, CallbackInfo ci) {
        if (HOLLOW_CAP.equals(entry.getAnnotationType())) ci.cancel();
        else return;

        ArrayList<Type> arr = (ArrayList<Type>) entry.getAnnotationData().get("value");

        final String targetName = entry.getMemberName().intern();

        cbs.computeIfAbsent(targetName, k -> new ArrayList<>()).add(new CapabilityBuilder(arr));
    }


}
