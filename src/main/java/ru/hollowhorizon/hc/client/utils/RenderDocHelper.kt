package ru.hollowhorizon.hc.client.utils

//? if forge
/*import net.minecraftforge.fml.loading.FMLConfig*/
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.common.config.HollowConfig
import ru.hollowhorizon.hc.common.config.hollowConfig

object RenderDocHelper {
    val config by hollowConfig(::Config, "hollowcore-renderdoc")

    @JvmStatic
    fun canAttachRenderdoc(): Boolean {
        if (!config.isEnabled) return false

        //? if fabric {
        return true
        //?} else {
        /*return !FMLConfig.getBoolConfigValue(FMLConfig.ConfigValue.EARLY_WINDOW_CONTROL)
        *///?}
    }

    @Serializable
    class Config : HollowConfig() {
        var isEnabled = false
    }
}