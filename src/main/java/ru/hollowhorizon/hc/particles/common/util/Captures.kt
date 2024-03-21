package ru.hollowhorizon.hc.particles.common.util

import ru.hollowhorizon.hc.particles.client.internal.RenderStateCapture
import net.minecraft.world.InteractionHand
import java.util.*

object Captures {
    val CAPTURES = EnumMap<InteractionHand, RenderStateCapture>(InteractionHand::class.java)
}