package ru.hollowhorizon.hc.client.gltf.animations

import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.animations.*
import kotlin.properties.Delegates


open class GLTFAnimationManager(val model: GltfModel) {
    val templates = AnimationType.load(model.model)
    private val nodeModels = model.model.walkNodes()
    private val bindPose = model.bindPose
    val layers = ArrayList<ILayer>()
    protected val animationCache = model.model.animations.associate {
        val name = it.name ?: "Unnamed"
        name to AnimationLoader.createAnimation(
            model.model,
            name
        )!!
    }
    private var smoothLayer = SmoothLayer(bindPose, null, null, 1.0f).apply {
        layers.add(this)
    }
    val hasCustomAnimation: Boolean
        get() = smoothLayer.second != null && smoothLayer.second!!.name !in templates.values
    var currentAnimation: String by Delegates.observable("") { _, oldValue, newValue ->
        if ((oldValue != newValue && newValue != "" && !hasCustomAnimation) || smoothLayer.shouldUpdate) {
            smoothLayer.shouldUpdate = false
            setSmoothAnimation(newValue, false)
        }
    }
    var currentTick = 0
    var hasHeadLayer = false

    val current: Animation?
        get() = this.smoothLayer.current

    fun updateEntity(entity: LivingEntity) {
        if (!hasHeadLayer) {
            hasHeadLayer = true
            this.addLayer(HeadLayer(entity, 1.0f))
        }
    }

    /**
     * Метод, обновляющий все анимации с учётом приоритетов
     */
    fun update(partialTick: Float) {
        layers.removeIf {
            it.update(this, partialTick)
            it.shouldRemove
        }

        nodeModels.parallelStream().forEach { node ->
            bindPose.apply(node, 0.0f) //Попробуем сбросить положение модели перед анимацией

            //для каждого канала в анимации (перемещение, поворот, размер, веса)
            AnimationTarget.values().forEach {
                applyTarget(node, it, partialTick) //рассчитываем все анимации и применяем
            }

        }
    }

    fun setTick(tick: Int) {
        this.currentTick = tick
    }

    private fun applyTarget(node: GltfTree.Node, target: AnimationTarget, time: Float) {
        var prioritySum = 0f
        val values = layers.map {
            val values = it.compute(node, target, time / 20f)
            if (values != null) prioritySum += it.priority
            it.priority to values
        }.sumWithPriority(prioritySum)

        when (target) {
            AnimationTarget.TRANSLATION -> node.transform.setTranslation(values ?: return)
            AnimationTarget.ROTATION -> node.transform.setRotation(values ?: return)
            AnimationTarget.SCALE -> node.transform.setScale(values ?: return)
        }
    }

    //Добавляет новую анимацию, плавно переходя от прошлой к этой
    fun setSmoothAnimation(animation: Animation, once: Boolean = false) {
        animation.reset(this)
        this.smoothLayer.push(animation)
        if (once) this.smoothLayer.playType = PlayType.ONCE
        else this.smoothLayer.playType = PlayType.LOOPED
    }

    fun setSmoothAnimation(animation: String, once: Boolean = false) {
        setSmoothAnimation(
            animationCache[animation] ?: throw AnimationException("Animation \"$animation\" not found!"),
            once
        )
    }

    //Добавляет новую анимацию, одновременно с остальными
    fun addLayer(animation: Animation, priority: Float = 1.0f) = addLayer(AnimationLayer(animation, priority))

    fun addLayer(layer: ILayer) {
        if (layers.contains(layer)) return
        layers.add(layer)
    }

    fun addAnimation(animation: String) {
        val anim = animationCache[animation] ?: throw AnimationException("Animation \"$animation\" not found!")
        anim.reset(this)
        addLayer(anim, 1.0f)
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
