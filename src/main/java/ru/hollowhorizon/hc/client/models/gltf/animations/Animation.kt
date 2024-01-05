package ru.hollowhorizon.hc.client.models.gltf.animations

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.Interpolator
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.QuatStep
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.Vec3Step

class Animation(val name: String, private val animationData: Map<GltfTree.Node, AnimationData>) {
    val maxTime = animationData.values.maxOf { it.maxTime }

    fun compute(node: GltfTree.Node, currentTime: Float): Transformation? {
        return animationData[node]?.let {
            val t = it.translation?.compute(currentTime)
            val r = it.rotation?.compute(currentTime)
            val s = it.scale?.compute(currentTime)

            node.toLocal(Transformation(t, r, s))
        }
    }

    override fun toString() = name

    companion object {
        fun createFromPose(model: List<GltfTree.Node>): Animation {
            return Animation("%BIND_POSE%", model.associate { node ->

                return@associate node to AnimationData(
                    node,
                    Vec3Step(
                        floatArrayOf(0f),
                        arrayOf(node.transform.translation)
                    ),
                    QuatStep(
                        floatArrayOf(0f),
                        arrayOf(node.transform.rotation)
                    ),
                    Vec3Step(
                        floatArrayOf(0f),
                        arrayOf(node.transform.scale)
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
    IDLE, IDLE_SNEAKED, WALK, WALK_SNEAKED, HURT,
    RUN, SWIM, FALL, FLY, SIT, SLEEP, SWING, DEATH;

    companion object {
        @JvmStatic
        fun load(model: GltfTree.GLTFTree): HashMap<AnimationType, String> {
            val names = model.animations.map { it.name ?: "Unnamed" }

            fun List<String>.findOr(vararg names: String) =
                this.find { anim -> names.any { anim.contains(it, ignoreCase = true) } }

            fun List<String>.findAnd(vararg names: String) =
                this.find { anim -> names.all { anim.contains(it, ignoreCase = true) } }

            val animations = hashMapOf<AnimationType, String>()

            animations[IDLE] = names.findOr("idle") ?: ""
            animations[IDLE_SNEAKED] = names.findAnd("idle", "sneak") ?: animations[IDLE] ?: ""
            animations[WALK] = names.minByOrNull {
                when {
                    it.contains("walk", ignoreCase = true) -> 0
                    it.contains("go", ignoreCase = true) -> 1
                    it.contains("run", ignoreCase = true) -> 2
                    it.contains("move", ignoreCase = true) -> 3
                    else -> 5
                }
            } ?: ""
            animations[HURT] = names.findOr("hurt", "damage") ?: ""
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

enum class AnimationState { STARTING, PLAYING, FINISHED }

@Serializable
enum class PlayMode {
    ONCE, //Одиночный запуск анимации
    LOOPED, //После завершения анимация начнётся с начала
    LAST_FRAME, //После завершения анимация застынет на последнем кадре
    REVERSED; //После завершения анимация начнёт проигрываться в обратном порядке


    fun stopOnEnd(): Boolean = this == ONCE
}

class AnimationData(
    val node: GltfTree.Node,
    val translation: Interpolator<Vector3f>?,
    val rotation: Interpolator<Quaternion>?,
    val scale: Interpolator<Vector3f>?,
) {
    val maxTime = maxOf(
        translation?.maxTime ?: 0f,
        rotation?.maxTime ?: 0f,
        scale?.maxTime ?: 0f,
    )
}

enum class AnimationTarget {
    TRANSLATION, ROTATION, SCALE;

    val numComponents: Int
        get() = if (this == ROTATION) 4 else 3
}