package ru.hollowhorizon.hc.mixin.groovy;

import groovy.lang.MetaClassImpl;
import org.codehaus.groovy.ast.ClassNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.scripting.sandbox.mapper.GroovyDeobfMapper;

@Mixin(value = MetaClassImpl.class, remap = false)
public abstract class MetaClassImplMixin {

    @Shadow protected abstract Object doInvokeMethod(Class sender, Object object, String methodName, Object[] originalArguments, boolean isCallToSuper, boolean fromInsideClass);

    @Shadow public abstract Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass);

    @Shadow @Final protected Class theClass;

    @Redirect(method = "invokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;ZZ)Ljava/lang/Object;", at = @At(value = "INVOKE", target = "Lgroovy/lang/MetaClassImpl;doInvokeMethod(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;ZZ)Ljava/lang/Object;"))
    public Object invokeMethod(MetaClassImpl instance, Class object, Object ownerClass, String methodName, Object[] args, boolean isCallToSuper, boolean fromInsideClass) {
        ClassNode owner = new ClassNode(ownerClass.getClass());

        if(owner.getPackageName().startsWith("net.minecraft") || owner.getPackageName().startsWith("com.mojang")) {

            String remappedName = GroovyDeobfMapper.getDeobfuscationMethodName(owner, methodName, args);

            HollowCore.LOGGER.info("trying to remap {}::{} to {}", owner.getNameWithoutPackage(), methodName, remappedName);

            return doInvokeMethod(object, ownerClass, remappedName, args, isCallToSuper, fromInsideClass);
        } else {
            return doInvokeMethod(object, ownerClass, methodName, args, isCallToSuper, fromInsideClass);
        }
    }

    @Inject(method = "getProperty(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;", at=@At("HEAD"), cancellable = true)
    public void getProperty(Object receiver, String field, CallbackInfoReturnable<Object> cir) {
        Class<?> clazz = (Class<?>) receiver;
        ClassNode owner = new ClassNode(clazz);

        if(owner.getPackageName().startsWith("net.minecraft") || owner.getPackageName().startsWith("com.mojang")) {

            String remappedName = GroovyDeobfMapper.getObfuscatedFieldName(clazz, field);

            HollowCore.LOGGER.info("trying to remap field {}::{} to {}", owner.getNameWithoutPackage(), field, remappedName);

            cir.setReturnValue(getProperty(theClass, receiver, remappedName, false, false));
        }
    }
}
