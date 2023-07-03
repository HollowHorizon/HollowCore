package ru.hollowhorizon.hc.mixin.kotlin;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.resolve.OverridingUtil;
import org.jetbrains.kotlin.types.KotlinType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.mappings.ClassMapping;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappings;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappingsKt;

import java.util.List;

@Mixin(value = OverridingUtil.class, remap = false)
public abstract class OverridingUtilMixin {

    @Shadow
    @Nullable
    private static OverridingUtil.OverrideCompatibilityInfo checkReceiverAndParameterCount(CallableDescriptor superDescriptor, CallableDescriptor subDescriptor) {
        return null;
    }

    @Shadow
    private static List<KotlinType> compiledValueParameters(CallableDescriptor callableDescriptor) {
        return null;
    }

    @Inject(method = "getBasicOverridabilityProblem", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/name/Name;equals(Ljava/lang/Object;)Z"), cancellable = true)
    private static void onOverrideMethod(CallableDescriptor superDescriptor, CallableDescriptor subDescriptor, CallbackInfoReturnable<OverridingUtil.OverrideCompatibilityInfo> cir) {
        DeclarationDescriptor superDesc = superDescriptor.getContainingDeclaration();
        JavaClass caller = null;
        if (superDesc instanceof LazyJavaStaticClassScopeAccessor) {
            caller = ((LazyJavaStaticClassScopeAccessor) superDesc).getJClass();
        } else if (superDesc instanceof LazyJavaClassMemberScopeAccessor) {
            caller = ((LazyJavaClassMemberScopeAccessor) superDesc).getJClass();
        } else if (superDesc instanceof LazyJavaClassDescriptorAccessor) {
            caller = ((LazyJavaClassDescriptorAccessor) superDesc).getJClass();
        }

        if (caller == null || caller.getFqName() == null) return;

        ClassMapping mapping = HollowMappings.INSTANCE.getMAPPINGS().get(caller);

        if (mapping == null) return;


        List<KotlinType> params = compiledValueParameters(subDescriptor);
        String signature = HollowMappingsKt.getSignature(params, subDescriptor.getReturnType());

        if (mapping.methodObf(subDescriptor.getName().asString(), signature).equals(superDescriptor.getName().asString())) {
            OverridingUtil.OverrideCompatibilityInfo receiverAndParameterResult = checkReceiverAndParameterCount(superDescriptor, subDescriptor);
            HollowCore.LOGGER.info("Remapping overrides. {}", receiverAndParameterResult);
            cir.setReturnValue(receiverAndParameterResult);
        }
    }
}
