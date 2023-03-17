package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.api.registy.HollowRegister
import ru.hollowhorizon.hc.client.models.core.BoneTownConstants
import ru.hollowhorizon.hc.client.models.core.animation.BTAdditionalAnimationEntry
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel
import ru.hollowhorizon.hc.client.models.core.model.BTModel
import ru.hollowhorizon.hc.client.utils.rl

object ModModels {
    @JvmField
    @HollowRegister
    val BIPED_IDLE = BTAdditionalAnimationEntry(
        "hc:biped".rl, "hc:biped_idle".rl
    )

    @JvmField
    @HollowRegister
    val BIPED_RUN = BTAdditionalAnimationEntry(
        "hc:biped".rl, "hc:biped_running".rl
    )

    @JvmField
    @HollowRegister
    val BIPED_BACKFLIP = BTAdditionalAnimationEntry(
        "hc:biped".rl, "hc:biped_backflip".rl
    )

    @JvmField
    @HollowRegister
    val BIPED_ZOMBIE_ARMS = BTAdditionalAnimationEntry(
        "hc:biped".rl, "hc:biped_zombie_arms".rl
    )

    @JvmField
    @HollowRegister
    val BIPED = BTAnimatedModel(BoneTownConstants.MeshTypes.BONEMF)

    @JvmField
    @HollowRegister
    val TEST_CUBE = BTModel(BoneTownConstants.MeshTypes.BONEMF)
}