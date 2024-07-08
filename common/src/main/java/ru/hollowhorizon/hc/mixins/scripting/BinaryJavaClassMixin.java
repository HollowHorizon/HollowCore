package ru.hollowhorizon.hc.mixins.scripting;

import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.impl.classFiles.BinaryClassSignatureParser;
import org.jetbrains.kotlin.load.java.structure.impl.classFiles.BinaryJavaClass;
import org.jetbrains.kotlin.load.java.structure.impl.classFiles.ClassifierResolutionContext;
import org.jetbrains.kotlin.name.FqName;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.scripting.ScriptingLogger;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappings;

@Mixin(value = BinaryJavaClass.class, remap = false)
public abstract class BinaryJavaClassMixin {
    @Final
    @Mutable
    @Shadow(remap = false)
    private FqName fqName;

    @Inject(method = "<init>(Lorg/jetbrains/kotlin/com/intellij/openapi/vfs/VirtualFile;Lorg/jetbrains/kotlin/name/FqName;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/ClassifierResolutionContext;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryClassSignatureParser;ILorg/jetbrains/kotlin/load/java/structure/JavaClass;[B)V", at = @At("TAIL"))
    private void onClassInit(VirtualFile virtualFile, FqName fqName, ClassifierResolutionContext context, BinaryClassSignatureParser signatureParser, int access, JavaClass outerClass, byte[] classContent, CallbackInfo ci) {

    }

    @ModifyArg(method = "visitMethod", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryJavaMethodBase$Companion;create(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lorg/jetbrains/kotlin/load/java/structure/JavaClass;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/ClassifierResolutionContext;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryClassSignatureParser;)Lkotlin/Pair;"), index = 0)
    private String onMethodCreating(String name) {
        if (!ForgeKotlinKt.isProduction()) return name;
        return name.startsWith("method_") ? HollowMappings.MAPPINGS.methodDeobf(name) : name;
    }

    @ModifyArg(method = "visitField", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/name/Name;identifier(Ljava/lang/String;)Lorg/jetbrains/kotlin/name/Name;"), index = 0)
    private String onFieldCreating(String name) {
        if (!ForgeKotlinKt.isProduction()) return name;
        return name.startsWith("field_") ? HollowMappings.MAPPINGS.fieldDeobf(name) : name;
    }
}