package ru.hollowhorizon.hc.common.objects.entities.animation

import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import ru.hollowhorizon.hc.client.models.core.animation.IPose
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.IAnimationLayer
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage
import java.util.function.Function

class AnimationState<T>(val name: String, val entity: T) where T : Entity, T : IBTAnimatedEntity<T> {
    private val layers = ArrayList<IAnimationLayer<T>>()
    private val layerIndex = HashMap<String, IAnimationLayer<T>>()
    private val skeleton = entity.skeleton
    private var bboxModifier: Function<AxisAlignedBB, AxisAlignedBB>? = null

    constructor(name: String, entity: T, bboxModifier: Function<AxisAlignedBB, AxisAlignedBB>?) : this(name, entity) {
        this.bboxModifier = bboxModifier
    }

    fun tickState(currentTicks: Int) {
        for (layer in layers) {
            if (layer.shouldRun()) {
                layer.tick(currentTicks)
            }
        }
    }

    fun applyStateToBoundingBox(boundingBox: AxisAlignedBB): AxisAlignedBB {
        return if (bboxModifier != null) {
            bboxModifier!!.apply(boundingBox)
        } else {
            boundingBox
        }
    }

    fun addLayer(layer: IAnimationLayer<T>) {
        layers.add(layer)
        layerIndex[layer.layerName] = layer
    }

    fun clearLayers() {
        layers.clear()
        layerIndex.clear()
    }

    fun consumeLayerMessage(layerName: String, message: AnimationLayerMessage) {
        layerIndex[layerName]?.consumeLayerMessage(message)
    }

    fun startLayer(name: String, currentTicks: Int) {
        layerIndex[name]?.apply {
            startTime = currentTicks
            isActive = true
        }
    }

    fun stopLayer(name: String) {
        layerIndex[name]?.isActive = false
    }

    fun leaveState() {
        layers.forEach { it.isActive = false}
    }

    fun enterState(startTime: Int) {
        for (layer in layers) {
            if (layer.autoStart) {
                layer.isActive = true
                layer.startTime = startTime
            }
        }
    }

    fun applyToPose(currentTicks: Int, partialTicks: Float, workFrame: IPose) {

        for (layer in layers) {
            layer.processLayer(skeleton.bindPose, currentTicks, partialTicks, workFrame)
        }

    }
}
