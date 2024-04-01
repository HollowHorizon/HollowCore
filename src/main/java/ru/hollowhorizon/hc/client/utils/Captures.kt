package ru.hollowhorizon.hc.client.utils

import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture
import net.minecraft.world.InteractionHand
import java.util.*

object Captures {
    val CAPTURES = EnumMap<InteractionHand, RenderStateCapture>(InteractionHand::class.java)
}