/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.gltf.animations

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.Interpolator

class Animation(val name: String, private val animationData: Map<GltfTree.Node, AnimationData>) {
    val maxTime = animationData.values.maxOf { it.maxTime }

    fun compute(node: GltfTree.Node, currentTime: Float): Transformation? {
        return animationData[node]?.let {
            val t = it.translation?.compute(currentTime)
            val r = it.rotation?.compute(currentTime)
            val s = it.scale?.compute(currentTime)
            val w = it.weights?.compute(currentTime)?.toList() ?: ArrayList()

            node.toLocal(Transformation(t, r, s, weights = w))
        }
    }

    override fun toString() = name

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

    val hasSpeed get() = this == RUN || this == WALK || this == WALK_SNEAKED

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
    val weights: Interpolator<FloatArray>?,
) {
    val maxTime = maxOf(
        translation?.maxTime ?: 0f,
        rotation?.maxTime ?: 0f,
        scale?.maxTime ?: 0f,
        weights?.maxTime ?: 0f
    )
}

enum class AnimationTarget {
    TRANSLATION, ROTATION, SCALE, WEIGHTS;

    val numComponents: Int
        get() = if (this == ROTATION) 4 else 3
}