package ru.hollowhorizon.hc.mixin.kotlin;

import net.minecraftforge.fml.ModList;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;

import java.io.File;

@Mixin(KotlinCoreEnvironment.Companion.class)
public class KotlinCoreEnvironmentMixin {

    /**
     * Specify the correct path to the compiler configuration (It is embedded in HollowCore)
     */
    @Redirect(method = "registerApplicationExtensionPointsAndExtensionsFrom", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/utils/PathUtil;getResourcePathForClass(Ljava/lang/Class;)Ljava/io/File;"), remap = false)
    private File getResourcePathForClass(Class<?> aClass) {
        return ModList.get().getModFileById("hc").getFile().getFilePath().toFile();
    }
}
