package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage
import java.util.function.Consumer

abstract class AnimationLayerBase<T>(override val layerName: String, override val entity: T) :
    IAnimationLayer<T> where T : Entity, T : IBTAnimatedEntity<T> {
    override var startTime = 0
    var isValid: Boolean = true
        protected set
    override var isActive = true

    override var autoStart = true
    override var duration: Int = -1
        protected set
    private val messageCallbacks = HashMap<String, Consumer<AnimationLayerMessage>>()
    private var animEndCallback: Runnable? = null

    override fun shouldLoop(): Boolean {
        return false
    }

    fun addMessageCallback(messageType: String, consumer: Consumer<AnimationLayerMessage>) {
        messageCallbacks[messageType] = consumer
    }

    override fun setEndCallback(callback: Runnable?) {
        animEndCallback = callback
    }

    override fun tick(tick: Int) {
        if (duration != -1 && animEndCallback != null) {
            var currentTicks = tick - startTime
            if (shouldLoop()) currentTicks %= duration
            if (currentTicks == duration - 1) animEndCallback!!.run()
        }
    }

    override fun shouldRun(): Boolean {
        return isValid && isActive
    }

    abstract fun doLayerWork(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose)

    override fun processLayer(basePose: IPose, currentTime: Int, partialTicks: Float, outPose: IPose) {
        if (!shouldRun()) {
            return
        }
        doLayerWork(basePose, currentTime, partialTicks, outPose)
    }

    override fun consumeLayerMessage(message: AnimationLayerMessage) {
        messageCallbacks[message.messageType]?.accept(message)
    }
}
