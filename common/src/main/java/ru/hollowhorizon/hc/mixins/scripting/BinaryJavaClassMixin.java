package ru.hollowhorizon.hc.mixins.scripting;

import org.jetbrains.kotlin.load.java.structure.impl.classFiles.BinaryJavaClass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappings;

@Mixin(value = BinaryJavaClass.class, remap = false)
public abstract class BinaryJavaClassMixin {

    @ModifyArg(method = "visitMethod", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryJavaMethodBase$Companion;create(Ljava/lang/String;ILjava/lang/String;Ljava/lang/String;Lorg/jetbrains/kotlin/load/java/structure/JavaClass;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/ClassifierResolutionContext;Lorg/jetbrains/kotlin/load/java/structure/impl/classFiles/BinaryClassSignatureParser;)Lkotlin/Pair;"), index = 0)
    private String onMethodCreating(String name) {
        if (!ForgeKotlinKt.isProduction()) return name;
        return name.startsWith("m_") ? HollowMappings.MAPPINGS.methodDeobf(name) : name;
    }

    @ModifyArg(method = "visitField", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/name/Name;identifier(Ljava/lang/String;)Lorg/jetbrains/kotlin/name/Name;"), index = 0)
    private String onFieldCreating(String name) {
        if (!ForgeKotlinKt.isProduction()) return name;
        return name.startsWith("f_") ? HollowMappings.MAPPINGS.fieldDeobf(name) : name;
    }
}
