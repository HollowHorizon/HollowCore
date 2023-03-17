package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.api.registy.HollowRegister
import ru.hollowhorizon.hc.client.models.core.materials.AnimatedMaterialEntry
import ru.hollowhorizon.hc.client.models.core.materials.BTMaterialEntry
import ru.hollowhorizon.hc.client.utils.rl


object BTMaterials {
    @JvmField
    val DEFAULT_STATIC_LOC = "hc:default_static".rl

    @JvmField
    val DEFAULT_ANIMATED_LOC = "hc:default_animated".rl

    @JvmField
    @HollowRegister("default_static")
    val STATIC_SHADER = BTMaterialEntry(
        "hc:shaders/materials/default_static.vs".rl,
        "hc:shaders/materials/default_static.fs".rl
    )

    @JvmField
    @HollowRegister("default_animated")
    val ANIMATED_SHADER = AnimatedMaterialEntry(
        "hc:shaders/materials/default_animated.vs".rl,
        "hc:shaders/materials/default_animated.fs".rl
    )
}