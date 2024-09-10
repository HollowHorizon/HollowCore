package ru.hollowhorizon.hc.mixins.scripting;

//? if forge {
/*import net.minecraftforge.fml.loading.FMLLoader;
*///?} elif neoforge {
/*import net.neoforged.fml.loading.FMLLoader;
*///?}
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.utils.PathUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScriptConfigurationKt;
import java.io.File;

@Mixin(KotlinCoreEnvironment.Companion.class)
public class KotlinCoreEnvironmentMixin {

    //? if forge || neoforge {
    /*@Redirect(method = "registerApplicationExtensionPointsAndExtensionsFrom", at = @At(value = "INVOKE", target = "Lorg/jetbrains/kotlin/utils/PathUtil;getResourcePathForClass(Ljava/lang/Class;)Ljava/io/File;"), remap = false)
    *///?}
    private File getResourcePathForClass(Class<?> aClass) {
        //? if forge || neoforge {
        /*if(!FMLLoader.isProduction()) return PathUtil.getResourcePathForClass(aClass);
        *///?}
        return HollowScriptConfigurationKt.compilerJar();
    }
}
