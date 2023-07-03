package ru.hollowhorizon.hc.mixin.kotlin;

import com.google.common.collect.Sets;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.jetbrains.kotlin.descriptors.PropertyDescriptor;
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor;
import org.jetbrains.kotlin.incremental.components.LookupLocation;
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaScope;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.storage.MemoizedFunctionToNotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.mappings.ClassMapping;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(value = LazyJavaScope.class, remap = false)
public abstract class LazyJavaScopeMixin {
    @Shadow
    public abstract Set<Name> getVariableNames();
    @Shadow
    public abstract Set<Name> getFunctionNames();

    @Final
    @Shadow
    private MemoizedFunctionToNotNull<Name, List<SimpleFunctionDescriptor>> functions;
    @Final
    @Shadow
    private MemoizedFunctionToNotNull<Name, List<PropertyDescriptor>> properties;

    /**
     * Redefining a variable name to get a static variable
     * <BR>
     * Like Vector3d.ZERO or Blocks.ANVIL
     */
    @Inject(method = "getContributedVariables", at = @At("HEAD"), cancellable = true)
    private void getContributedVariables(Name name, LookupLocation location, CallbackInfoReturnable<Collection<PropertyDescriptor>> cir) {
        if (!FMLEnvironment.production) return;

        JavaClass caller = null;
        if (this instanceof LazyJavaStaticClassScopeAccessor) {
            caller = ((LazyJavaStaticClassScopeAccessor) this).getJClass();
        } else if (this instanceof LazyJavaClassMemberScopeAccessor) {
            caller = ((LazyJavaClassMemberScopeAccessor) this).getJClass();
        }

        ClassMapping mapping = HollowMappings.INSTANCE.getMAPPINGS().get(caller);

        if (mapping == null) return;

        Name obfName = Name.identifier(mapping.fieldObf(name.asString()));

        if (obfName.asString().equals(name.asString())) return;

        HollowCore.LOGGER.info("[Remapper] Remapping field {} to {}", name.asString(), obfName.asString());

        if (!getVariableNames().contains(obfName)) return;

        cir.setReturnValue(properties.invoke(obfName));
    }

    @Inject(method = "getContributedFunctions", at = @At("HEAD"), cancellable = true)
    private void getContributedFunctions(Name name, LookupLocation location, CallbackInfoReturnable<Collection<SimpleFunctionDescriptor>> cir) {
        if (!FMLEnvironment.production) return;

        JavaClass caller = null;
        if (this instanceof LazyJavaStaticClassScopeAccessor) {
            caller = ((LazyJavaStaticClassScopeAccessor) this).getJClass();
        } else if (this instanceof LazyJavaClassMemberScopeAccessor) {
            caller = ((LazyJavaClassMemberScopeAccessor) this).getJClass();
        }

        ClassMapping mapping = HollowMappings.INSTANCE.getMAPPINGS().get(caller);

        if (mapping == null) return;

        List<String> methods = mapping.methodsObf(name.asString());
        List<SimpleFunctionDescriptor> descriptors = new ArrayList<>();

        for (String method : methods) {
            Name identifier = Name.identifier(method);

            if (!getFunctionNames().contains(identifier)) continue;

            if (identifier.asString().equals(name.asString())) continue;

            HollowCore.LOGGER.info("[Remapper] Remapping function {} to {}", name.asString(), identifier.asString());

            descriptors.addAll(functions.invoke(identifier));
        }

        cir.setReturnValue(descriptors);
    }
}
