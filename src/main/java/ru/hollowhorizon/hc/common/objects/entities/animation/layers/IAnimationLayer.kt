package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage

interface IAnimationLayer<T> where T : Entity?, T : IBTAnimatedEntity<T> {
    val entity: T
    val duration: Int
    val layerName: String

    var isActive: Boolean
    var autoStart: Boolean
    var startTime: Int

    fun processLayer(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose)

    fun shouldRun(): Boolean
    fun tick(tick: Int)
    fun shouldLoop(): Boolean
    fun setEndCallback(callback: Runnable?)
    fun consumeLayerMessage(message: AnimationLayerMessage)
}