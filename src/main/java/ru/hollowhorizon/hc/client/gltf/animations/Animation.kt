package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.GltfModel
import de.javagl.jgltf.model.NodeModel
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability

class Animation(val name: String, val animationData: Map<NodeModel, List<AnimationData>>) {
    val maxTime = animationData.maxOf { data -> data.value.maxOfOrNull { it.interpolator.maxTime } ?: 0f }.let {
        if (it == 0f) return@let 1f else it
    }

    fun hasNode(node: NodeModel) = animationData.contains(node)

    fun compute(node: NodeModel, priority: Float, time: Float): Map<AnimationTarget, FloatArray> {
        return animationData[node]?.associate { anim ->
            anim.target to anim.interpolator.compute(time, priority)
        } ?: emptyMap()
    }

    override fun toString() = name

    companion object {
        fun createFromPose(model: List<NodeModel>): Animation {
            return Animation("%BIND_POSE%", model.associate { node ->
                val list = arrayListOf<AnimationData>()
                if (node.translation != null) list.add(AnimationData(AnimationTarget.TRANSLATION, Interpolator.Step(floatArrayOf(0f), arrayOf(node.translation))))
                if (node.rotation != null) list.add(AnimationData(AnimationTarget.ROTATION, Interpolator.Step(floatArrayOf(0f), arrayOf(node.rotation))))
                if (node.scale != null) list.add(AnimationData(AnimationTarget.SCALE, Interpolator.Step(floatArrayOf(0f), arrayOf(node.scale))))
                if (node.weights != null) list.add(AnimationData(AnimationTarget.WEIGHTS, Interpolator.Step(floatArrayOf(0f), arrayOf(node.weights))))
                return@associate node to list
            })
        }
    }
}

enum class AnimationType {
    IDLE, IDLE_SNEAKED, WALK, WALK_SNEAKED,
    RUN, SWIM, FALL, FLY, SIT, SLEEP, SWING, DEATH;

    companion object {
        @JvmStatic
        fun load(model: GltfModel, capability: AnimatedEntityCapability) {
            val names = model.animationModels.map { it.name }

            fun List<String>.findOr(vararg names: String) = this.find { anim -> names.any { anim.contains(it) } }
            fun List<String>.findAnd(vararg names: String) = this.find { anim -> names.all { anim.contains(it) } }

            with(capability) {
                animations[IDLE] = names.findOr("idle") ?: ""
                animations[IDLE_SNEAKED] = names.findAnd("idle", "sneak") ?: capability.animations[IDLE] ?: ""
                animations[WALK] = names.minByOrNull {
                    when {
                        it.contains("walk") -> 0
                        it.contains("go") -> 1
                        it.contains("run") -> 2
                        it.contains("move") -> 3
                        else -> 4
                    }
                } ?: ""
                animations[WALK_SNEAKED] = names.findAnd("walk", "sneak") ?: capability.animations[WALK] ?: ""
                animations[RUN] = names.findOr("run", "flee", "dash") ?: capability.animations[WALK] ?: ""
                animations[SWIM] = names.findOr("swim") ?: capability.animations[WALK] ?: ""
                animations[FALL] = names.findOr("fall") ?: capability.animations[IDLE] ?: ""
                animations[FLY] = names.findOr("fly") ?: capability.animations[IDLE] ?: ""
                animations[SIT] = names.findOr("sit") ?: capability.animations[IDLE] ?: ""
                animations[SLEEP] = names.findOr("sleep") ?: capability.animations[SLEEP] ?: ""
                animations[SWING] = names.findOr("attack", "swing") ?: ""
                animations[DEATH] = names.findOr("death") ?: ""

            }
        }
    }
}

enum class PlayType {
    ONCE, //Одиночный запуск анимации
    LOOPED, //После завершения анимация начнётся с начала
    LAST_FRAME, //После завершения анимация застынет на последнем кадре
    PING_PONG; //После завершения анимация начнёт проигрываться в обратном порядке


    fun stopOnEnd(): Boolean = this == ONCE
}

class AnimationData(val target: AnimationTarget, val interpolator: Interpolator<*>)

enum class AnimationTarget { TRANSLATION, ROTATION, SCALE, WEIGHTS }