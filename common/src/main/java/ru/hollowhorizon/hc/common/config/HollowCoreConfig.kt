package ru.hollowhorizon.hc.common.config

import kotlinx.serialization.Serializable

@Serializable
class HollowCoreConfig : HollowConfig() {
    @Serializable
    val debugMode by property(false)
}