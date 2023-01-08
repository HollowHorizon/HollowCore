package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.fml.ModList;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.File;

@Mixin(KotlinCoreEnvironment.Companion.class)
public class PathUtilMixin {

    @Redirect(method = "registerApplicationExtensionPointsAndExtensionsFrom", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/utils/PathUtil;getResourcePathForClass(Ljava/lang/Class;)Ljava/io/File;"), remap = false)
    private File getResourcePathForClass(Class<?> aClass) {
        System.out.println("PathUtilMixin.getResourcePathForClass: " + aClass);
        return ModList.get().getModFileById("hc").getFile().getFilePath().toFile();
    }
}
