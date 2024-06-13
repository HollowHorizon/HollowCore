package ru.hollowhorizon.hc.common.config

import kotlinx.serialization.Serializable

@Serializable
class HollowCoreConfig : HollowConfig() {
    val debugMode = false
}