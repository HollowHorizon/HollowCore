package ru.hollowhorizon.hc.client.gltf.animations

import com.modularmods.mcgltf.RenderedGltfModel
import de.javagl.jgltf.model.NodeModel
import kotlin.properties.Delegates


open class GLTFAnimationManager(val model: RenderedGltfModel) {
    val templates = AnimationType.load(model.gltfModel)
    private val nodeModels = model.gltfModel.nodeModels
    private val bindPose = model.gltfModel.bindPose
    val layers = ArrayList<ILayer>()
    protected val animationCache = model.gltfModel.animationModels.associate {
        it.name to AnimationLoader.createAnimation(
            model.gltfModel,
            it.name
        )!!
    }
    private var smoothLayer = SmoothLayer(bindPose, null, null, 1.0f).apply {
        layers.add(this)
    }
    var currentAnimation: String by Delegates.observable("") { _, oldValue, newValue ->
        if (oldValue != newValue && newValue != "") setLivingAnimation(newValue)
    }

    val current: Animation?
        get() = this.smoothLayer.current

    /**
     * Метод, обновляющий все анимации с учётом приоритетов
     */
    fun update(partialTick: Float) {
        layers.removeIf {
            it.update(partialTick)
            it.shouldRemove
        }

        nodeModels.forEach { node ->
            bindPose.apply(node, 0.0f) //Попробуем сбросить положение модели перед анимацией

            //для каждого канала в анимации (перемещение, поворот, размер, веса)
            AnimationTarget.entries.forEach {
                applyTarget(node, it, partialTick) //рассчитываем все анимации и применяем
            }

        }
    }

    private fun applyTarget(node: NodeModel, target: AnimationTarget, time: Float) {
        var prioritySum = 0f
        val values = layers.map {
            val values = it.compute(node, target, time / 20f)
            if (values != null) prioritySum += it.priority
            it.priority to values
        }.sumWithPriority(prioritySum)

        when (target) {
            AnimationTarget.TRANSLATION -> node.translation = values ?: return
            AnimationTarget.ROTATION -> node.rotation = values ?: return
            AnimationTarget.SCALE -> node.scale = values ?: return
            AnimationTarget.WEIGHTS -> node.weights = values ?: return
        }
    }

    //Добавляет новую анимацию, плавно переходя от прошлой к этой
    fun setLivingAnimation(animation: Animation) {
        this.smoothLayer.push(animation)
    }

    fun setLivingAnimation(animation: String) {
        setLivingAnimation(animationCache[animation] ?: throw AnimationException("Animation \"$animation\" not found!"))
    }

    //Добавляет новую анимацию, одновременно с остальными
    fun addLayer(animation: Animation, priority: Float = 1.0f) = addLayer(AnimationLayer(animation, priority))

    fun addLayer(layer: ILayer) {
        if (layers.contains(layer)) return
        layers.add(layer)
    }

    fun addAnimation(animation: String) {
        addLayer(animationCache[animation] ?: throw AnimationException("Animation \"$animation\" not found!"), 3.0f)
    }

    fun removeLayer(layer: ILayer) = layers.remove(layer)
    fun removeAnimation(animation: String) {
        this.layers.removeIf { (it as? AnimationLayer)?.animation?.name == animation }
    }
}

fun List<Pair<Float, FloatArray?>>.sumWithPriority(prioritySum: Float): FloatArray? {
    if (this.isEmpty()) return null
    if (this.size == 1) return this.first().second

    val result = FloatArray(this.firstNotNullOfOrNull { it.second }?.size ?: return null)

    this.forEach { entry ->
        val array = entry.second ?: return@forEach

        for (i in array.indices) result[i] += array[i] * entry.first //value * priority
    }

    return result.apply {
        for (i in this.indices) this[i] /= prioritySum
    }
}
