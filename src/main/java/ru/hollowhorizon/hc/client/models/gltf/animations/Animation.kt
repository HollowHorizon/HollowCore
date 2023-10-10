package ru.hollowhorizon.hc.client.models.gltf.animations

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import ru.hollowhorizon.hc.client.gltf.animations.GLTFAnimationManager
import ru.hollowhorizon.hc.client.models.gltf.GltfTree

class Animation(val name: String, val animationData: Map<GltfTree.Node, AnimationData>) {
    var startTime = 0
    var currentTime = 0f
    var isEnded = false
    val maxTime = animationData.maxOf { it.value.maxTime }.let {
        if (it == 0f) 0.0001f else it
    }

    fun reset(manager: GLTFAnimationManager) {
        startTime = manager.currentTick
        currentTime = 0f
        isEnded = false
    }

    fun hasNode(node: GltfTree.Node, target: AnimationTarget): Boolean {
        val data = animationData.entries.find { it.key.index == node.index }?.value ?: return false

        return when (target) {
            AnimationTarget.TRANSLATION -> data.translation != null
            AnimationTarget.ROTATION -> data.rotation != null
            AnimationTarget.SCALE -> data.scale != null
        }
    }

    fun update(layer: ILayer, manager: GLTFAnimationManager, partialTick: Float) {

        when (layer.playType) {
            PlayType.ONCE -> {
                if (currentTime < maxTime) currentTime = (manager.currentTick - startTime + partialTick) / 20 * layer.speed
                else isEnded = true
            }

            PlayType.LOOPED -> {
                currentTime = ((manager.currentTick - startTime + partialTick) / 20 * layer.speed) % maxTime
            }

            PlayType.LAST_FRAME -> {
                currentTime = if (currentTime < maxTime) (manager.currentTick - startTime + partialTick) / 20 * layer.speed
                else maxTime
            }

            PlayType.REVERSED -> {
                //currentTime = ((manager.currentTick - startTime + partialTick) / 20 * layer.speed) % maxTime
                //if (currentTime > maxTime || currentTime < 0f) layer.speed *= -1
                TODO("Make it works")
            }
        }
    }

    fun compute(node: GltfTree.Node, target: AnimationTarget): FloatArray? {
        return animationData.entries.find { it.key.index == node.index }?.value?.let {
            when (target) {
                AnimationTarget.TRANSLATION -> it.translation?.compute(currentTime)
                AnimationTarget.ROTATION -> it.rotation?.compute(currentTime)
                AnimationTarget.SCALE -> it.scale?.compute(currentTime)
            }
        }
    }

    fun apply(node: GltfTree.Node, time: Float) {
        animationData[node]?.let { anim ->
            node.transform.setTranslation(anim.translation?.compute(time))
            node.transform.setRotation(anim.rotation?.compute(time))
            node.transform.setScale(anim.scale?.compute(time))
        }
    }

    override fun toString() = name

    companion object {
        fun createFromPose(model: List<GltfTree.Node>): Animation {
            return Animation("%BIND_POSE%", model.associate { node ->

                return@associate node to AnimationData(
                    node,
                    Interpolator.Step(
                        floatArrayOf(0f),
                        arrayOf(node.transform.translation.array())
                    ),
                    Interpolator.Step(
                        floatArrayOf(0f),
                        arrayOf(node.transform.rotation.array())
                    ),
                    Interpolator.Step(
                        floatArrayOf(0f),
                        arrayOf(node.transform.scale.array())
                    ),
                )
            })
        }
    }
}

fun Vector3f.array(): FloatArray {
    return floatArrayOf(x(), y(), z())
}
fun Quaternion.array(): FloatArray {
    return floatArrayOf(i(), j(), k(), r())
}

fun Vector4f.array(): FloatArray {
    return floatArrayOf(x(), y(), z(), w())
}

enum class AnimationType {
    IDLE, IDLE_SNEAKED, WALK, WALK_SNEAKED,
    RUN, SWIM, FALL, FLY, SIT, SLEEP, SWING, DEATH;

    companion object {
        @JvmStatic
        fun load(model: GltfTree.GLTFTree): HashMap<AnimationType, String> {
            val names = model.animations.map { it.name ?: "Unnamed" }

            fun List<String>.findOr(vararg names: String) = this.find { anim -> names.any { anim.contains(it) } }
            fun List<String>.findAnd(vararg names: String) = this.find { anim -> names.all { anim.contains(it) } }
            val animations = hashMapOf<AnimationType, String>()

            animations[IDLE] = names.findOr("idle") ?: ""
            animations[IDLE_SNEAKED] = names.findAnd("idle", "sneak") ?: animations[IDLE] ?: ""
            animations[WALK] = names.minByOrNull {
                when {
                    it.contains("walk") -> 0
                    it.contains("go") -> 1
                    it.contains("run") -> 2
                    it.contains("move") -> 3
                    else -> 4
                }
            } ?: ""
            animations[WALK_SNEAKED] = names.findAnd("walk", "sneak") ?: animations[WALK] ?: ""
            animations[RUN] = names.findOr("run", "flee", "dash") ?: animations[WALK] ?: ""
            animations[SWIM] = names.findOr("swim") ?: animations[WALK] ?: ""
            animations[FALL] = names.findOr("fall") ?: animations[IDLE] ?: ""
            animations[FLY] = names.findOr("fly") ?: animations[IDLE] ?: ""
            animations[SIT] = names.findOr("sit") ?: animations[IDLE] ?: ""
            animations[SLEEP] = names.findOr("sleep") ?: animations[SLEEP] ?: ""
            animations[SWING] = names.findOr("attack", "swing", "use") ?: ""
            animations[DEATH] = names.findOr("death") ?: ""

            return animations
        }
    }
}

enum class PlayType {
    ONCE, //Одиночный запуск анимации
    LOOPED, //После завершения анимация начнётся с начала
    LAST_FRAME, //После завершения анимация застынет на последнем кадре
    REVERSED; //После завершения анимация начнёт проигрываться в обратном порядке


    fun stopOnEnd(): Boolean = this == ONCE
}

class AnimationData(
    val node: GltfTree.Node,
    val translation: Interpolator<*>?,
    val rotation: Interpolator<*>?,
    val scale: Interpolator<*>?,
) {
    val maxTime = maxOf(
        translation?.maxTime ?: 0f,
        rotation?.maxTime ?: 0f,
        scale?.maxTime ?: 0f,
    )
}

enum class AnimationTarget { TRANSLATION, ROTATION, SCALE;
    val numComponents: Int
        get() = if(this == ROTATION) 4 else 3
}