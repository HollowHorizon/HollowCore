package ru.hollowhorizon.hc.mixins.scripting;

import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.common.scripting.ScriptingLogger;
import ru.hollowhorizon.hc.common.scripting.mappings.HollowMappings;

@Mixin(value = ClassId.class, remap = false)
public class ClassIdMixin {
    @Shadow(remap = false)
    @Final
    @Mutable
    private FqName relativeClassName;

    @Shadow(remap = false)
    @Final
    @Mutable
    private FqName packageFqName;


    @Inject(method = "<init>(Lorg/jetbrains/kotlin/name/FqName;Lorg/jetbrains/kotlin/name/FqName;Z)V", at = @At("TAIL"))
    private void onLoadClass(FqName packageName, FqName relativeName, boolean isLocal, CallbackInfo ci) {
        if (!ForgeKotlinKt.isProduction()) return;
        var name = packageName.asString() + "." + relativeName.asString();
        var isInner = relativeName.asString().contains("$");
        var remapped = HollowMappings.MAPPINGS.classObf(name).replace('/', '.');
        if (isInner) {
            var index = remapped.lastIndexOf('.');
            remapped = remapped.substring(0, index) + "$" + remapped.substring(index + 1);
        }
        if (!name.equals(remapped)) {
            var index = remapped.lastIndexOf('.');
            var newPackage = remapped.substring(0, index);
            var newName = remapped.substring(index + 1);

            ScriptingLogger.INSTANCE.getLOGGER().info(String.format("Remapping Class: %s -> %s [%s] [%s]", name, remapped, newPackage, newName));

            relativeClassName = new FqName(newName);
            packageFqName = new FqName(newPackage);
        }
    }
}
