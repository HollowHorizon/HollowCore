package ru.hollowhorizon.hc.client.gltf.animations

import net.minecraftforge.fml.server.ServerLifecycleHooks
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel

class AnimationManager(val model: RenderedGltfModel) {
    //Слой, который добавляет плавные переходы между 2 анимациями
    private val animationCache = model.gltfModel.animationModels.associate {
        it.name to AnimationLoader.createAnimation(
            model.gltfModel,
            it.name
        )!!
    }
    private val nodeModels = model.gltfModel.nodeModels
    private val layers = ArrayList<ILayer>().apply {
        smoothLayer = SmoothLayer(null, null, 1.0f)
        this.add(smoothLayer)

        //Без этого почему-то иногда ломаются анимации ._.
        this.add(AnimationLayer(Animation.createFromPose(nodeModels), 100.0f))
    }
    private var time = 0f
    private var partialTickO = 0f
    private var smoothLayer: SmoothLayer
    val current: Animation?
        get() = this.smoothLayer.current

    fun update(partialTick: Float) {

        if (partialTick >= partialTickO) time++
        partialTickO = partialTick

        layers.removeIf { it.priority <= 0.0f }

        nodeModels.forEach { node ->
            var prioritySum = 0f
            val frames = layers.map {
                it.update(partialTick)
                val values = it.compute(node, (time + partialTick) / 60f)
                if (values.isNotEmpty()) prioritySum += it.priority
                values
            }.flatMap { it.entries }.groupBy({ it.key }, { it.value })

            frames.forEach { (target, values) ->
                val array = values.sumWithPriority(prioritySum)

                when (target) {
                    AnimationTarget.TRANSLATION -> node.translation = array
                    AnimationTarget.ROTATION -> node.rotation = array
                    AnimationTarget.SCALE -> node.scale = array
                    AnimationTarget.WEIGHTS -> node.weights = array
                }
            }
        }
    }

    //Добавляет новую анимацию, плавно переходя от прошлой к этой
    fun setAnimation(animation: Animation) {
        this.smoothLayer.push(animation)
    }

    fun setAnimation(animation: String) {
        setAnimation(animationCache[animation] ?: throw AnimationException("Animation $animation not found!"))
    }

    //Добавляет новую анимацию, одновременно с остальными
    fun addLayer(animation: Animation, priority: Float = 1.0f) = addLayer(AnimationLayer(animation, priority))

    fun addLayer(layer: ILayer) = layers.add(layer)

    fun removeLayer(layer: ILayer) = layers.remove(layer)
}

fun List<FloatArray>.sumWithPriority(prioritySum: Float): FloatArray {
    if (this.isEmpty()) return FloatArray(0)

    val result = FloatArray(this[0].size)

    this.forEach { array ->
        for (i in array.indices) result[i] += array[i]
    }

    return result.apply {
        for (i in this.indices) this[i] /= prioritySum
    }
}
