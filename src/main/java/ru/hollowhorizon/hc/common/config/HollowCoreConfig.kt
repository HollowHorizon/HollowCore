package ru.hollowhorizon.hc.common.config

import imgui.ImGui
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toTexture

@Serializable
class HollowCoreConfig : HollowConfig() {
    val debugMode = false

    val inventory = InventoryConfig()
    val scripting = Scripting()
}

@Serializable
class InventoryConfig {
    var enableItemCounts = true
    var enableItemRotation = true
}

@Serializable
class Scripting {
    var includeMods = mutableListOf("hollowcore", "hollowengine") //+ platformMods
}

private val platformMods = when (HollowCore.platform) {
    HollowCore.Platform.FABRIC -> arrayOf("fabric-api")
    HollowCore.Platform.FORGE -> arrayOf("forge")
    HollowCore.Platform.NEOFORGE -> arrayOf("neoforge")
}
