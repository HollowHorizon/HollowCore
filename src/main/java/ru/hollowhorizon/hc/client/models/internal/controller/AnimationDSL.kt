package ru.hollowhorizon.hc.client.models.internal.controller

import de.fabmax.kool.math.QuatF
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.scene.TrsTransformF
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import ru.hollowhorizon.hc.client.gui.DebugOverlay
import ru.hollowhorizon.hc.client.models.internal.AnimatedModel
import ru.hollowhorizon.hc.client.models.internal.Node
import ru.hollowhorizon.hc.client.models.internal.animations.Animation
import ru.hollowhorizon.hc.common.utils.molang.EntityQuery
import ru.hollowhorizon.hc.common.utils.molang.Molang
import ru.hollowhorizon.hc.common.utils.molang.MolangCompilerScope
import java.util.*

private fun Float.modPositive(divisor: Float): Float =
    ((this % divisor) + divisor) % divisor

enum class WrapMode(val wrapTime: (Animation, Float) -> Float) {
    Once({ animation, time ->
        if (time < 0) animation.maxTime + time else time
    }),
    Loop({ animation, time ->
        time.modPositive(animation.maxTime)
    }),
    PingPong({ animation, time ->
        val period = animation.maxTime * 2
        val m = time.modPositive(period)
        if (m <= animation.maxTime) m else period - m
    }),
    Reverse({ animation, time ->
        if (time < 0) time else animation.maxTime - time
    }),
    ClampForever({ animation, time ->
        if (time < 0) animation.maxTime - (-time).coerceAtMost(animation.maxTime)
        else time.coerceAtMost(animation.maxTime)
    });
}

enum class BlendMode { Override, Additive }

@Serializable
data class Mask(val bones: Set<String>) {
    companion object {
        fun full() = Mask(emptySet())
        fun of(vararg bones: String) = Mask(bones.toSet())
    }
}

data class Controller(var layers: List<Layer>) {
    init {
        layers = layers.sortedBy { it.priority }
    }

    private val initDeferred = MolangCompilerScope.async { init() }
    private var isReady = false

    suspend fun init() {
        layers.forEach { it.stateMachine.init() }
        isReady = true
    }

    fun uploadAnimations(animations: Map<String, Animation>) {
        layers.forEach { it.stateMachine.states.forEach { it.animations = animations } }
    }

    fun update(node: Node, query: EntityQuery, time: Float) {
        if (!initDeferred.isCompleted) return
        layers.forEach { layer ->
            if (node.name !in layer.mask.bones && layer.mask.bones.isNotEmpty()) return@forEach

            layer.update(node, query, time)?.let { transform ->
                node.transform
                    .translate(Vec3f.ZERO.mix(transform.translation, layer.weight))
                    .rotate(QuatF.IDENTITY.mix(transform.rotation, layer.weight))
                    .scale(Vec3f.ONES.mix(transform.scale, layer.weight))
            }
        }
    }

    fun updateProcedural(model: AnimatedModel, query: EntityQuery) {
        if (!isReady) return
        layers.forEach {
            it.updateProcedural(model, query)
        }
    }
}

fun animationController(block: ControllerBuilder.() -> Unit): Controller =
    ControllerBuilder().apply(block).build()

class ControllerBuilder {
    private val layers = mutableListOf<Layer>()

    fun layer(
        name: String,
        priority: Int = 0,
        weight: Float = 1f,
        mask: Mask = Mask.full(),
        block: LayerBuilder.() -> Unit,
    ) {
        require(layers.none { it.name == name }) { "Layer '$name' already defined" }
        layers += LayerBuilder(name, priority, weight, mask).apply(block).build()
        layers.sortByDescending { it.priority }
    }

    fun build(): Controller = Controller(layers)
}

@Serializable
data class Layer(
    val name: String,
    val priority: Int,
    val weight: Float,
    val mask: Mask,
    val stateMachine: StateMachine,
) {
    fun update(node: Node, query: EntityQuery, time: Float): TrsTransformF? {
        return stateMachine.update(node, query, time)
    }

    fun updateProcedural(model: AnimatedModel, query: EntityQuery) {
        stateMachine.updateProcedural(model, query)
    }
}

class LayerBuilder(
    val name: String,
    val priority: Int,
    val weight: Float,
    val mask: Mask,
) {
    private var stateMachine = StateMachineBuilder().build()
    fun stateMachine(block: StateMachineBuilder.() -> Unit) {
        stateMachine = StateMachineBuilder().apply(block).build()
    }

    fun build() = Layer(name, priority, weight, mask, stateMachine)
}

// State Machine

@Serializable
data class State(
    val name: String,
    val clip: ClipNode?,
    val blendTree: BlendTree?,
    val procedural: ProceduralNode?,
) {
    @Transient
    var animations: Map<String, Animation> = mutableMapOf()

    fun reset(query: EntityQuery, time: Float) {
        clip?.reset(query, time)
        blendTree?.reset(query, time)
    }

    fun update(node: Node, query: EntityQuery, time: Float): TrsTransformF? {
        clip?.let { clip ->
            return clip.update(animations, query, node, time)
        }
        blendTree?.let {
            return it.update(animations, query, node, time)
        }
        return null
    }

    suspend fun init() {
        clip?.init()
        blendTree?.init()
        procedural?.init()
    }

}

@Serializable
data class Transition(
    val from: State,
    val to: State,
    private val function: String,
    val duration: Float,
    val transitionClip: ClipNode?,
    val deltaClip: DeltaNode?,
) {
    @Transient
    lateinit var condition: (EntityQuery) -> Boolean

    @Transient
    private val conditionDeferred = MolangCompilerScope.async {
        Molang.compileBoolean(function)
    }

    suspend fun init() {
        condition = conditionDeferred.await()
    }

    var startTime = 0f
    var isFinished = false

    fun start(query: EntityQuery, time: Float) {
        startTime = time
        isFinished = false
        to.reset(query, time)
    }

    fun update(node: Node, query: EntityQuery, time: Float): TrsTransformF? {
        val from = from.update(node, query, time)
        val to = to.update(node, query, time)

        val factor = ((time - startTime) / duration).coerceIn(0f..1f)
        if (factor == 1f) isFinished = true

        return from.mix(to, factor)
    }
}

fun TrsTransformF?.mix(other: TrsTransformF?, factor: Float): TrsTransformF? {
    if (this == null) return other?.let { TrsTransformF().mix(it, factor) }
    if (other == null) return this.mix(TrsTransformF(), factor)

    setCompositionOf(
        translation.mix(other.translation, factor),
        rotation.mix(other.rotation, factor),
        scale.mix(other.scale, factor)
    )
    return this
}

@Serializable
data class StateMachine(
    val states: List<State>,
    val transitions: List<Transition>,
) {
    var currentState: State? = null
    var currentTransition: Transition? = null

    suspend fun init() {
        states.forEach { it.init() }
        transitions.forEach { it.init() }
    }

    fun update(node: Node, query: EntityQuery, time: Float): TrsTransformF? {
        if (currentTransition == null) {
            transitions.firstOrNull { (it.from == currentState || currentState == null) && it.condition(query) }
                ?.let {
                    currentTransition = it
                    it.start(query, time)
                }

            if (transitions.isEmpty() && currentState == null) currentState = states.firstOrNull()
        }

        currentTransition?.let { transition ->
            if (transition.isFinished) {
                currentTransition = null
                currentState = transition.to
                return@let
            }
            return transition.update(node, query, time)
        }

        DebugOverlay.debugText["State"] = StringBuilder().apply {
            append("Moving: ${query.is_moving}\n")
            append("Sneaking: ${query.is_sneaking}\n")
            append("Speed: ${query.ground_speed}")
        }.toString()
        return currentState?.update(node, query, time)
    }

    fun updateProcedural(model: AnimatedModel, query: EntityQuery) {
        currentState?.let { state ->
            state.procedural?.let { procedural ->
                procedural.evaluate(model, query)
            }
        }
    }
}

class StateMachineBuilder() {
    private val states = mutableListOf<State>()
    private val transitions = mutableListOf<Transition>()

    fun state(name: String, block: StateBuilder.() -> Unit): State {
        val state = StateBuilder(name).apply(block).build()
        states += state
        return state
    }

    fun transition(from: State, to: State, block: TransitionBuilder.() -> Unit) {
        transitions += TransitionBuilder(from, to).apply(block).build()
    }

    fun build() = StateMachine(states, transitions)
}

class StateBuilder(val name: String) {
    private var clip: ClipNode? = null
    private var blendTree: BlendTree? = null
    private var procedural: ProceduralNode? = null

    fun clip(
        name: String,
        wrap: WrapMode = WrapMode.Loop,
        blend: BlendMode = BlendMode.Override,
        speed: String = "1f",
    ) {
        clip = ClipNode(name, wrap, blend, speed)
    }

    fun blendTree(block: BlendTreeBuilder.() -> Unit) {
        blendTree = BlendTreeBuilder().apply(block).build()
    }

    fun procedural(block: ProceduralBuilder.() -> Unit) {
        procedural = ProceduralBuilder().apply(block).build()
    }

    fun build() = State(name, clip, blendTree, procedural)
}

// BlendTree


@Serializable
data class BlendTree(
    private val function: String,
    val nodes: List<BlendNode>,
    private val smoothingTime: Float = 0f,
) {
    @Transient
    private lateinit var factor: (EntityQuery) -> Float

    @Transient
    private val factorDeferred = MolangCompilerScope.async {
        Molang.compileFloat(function)
    }

    suspend fun init() {
        factor = factorDeferred.await()
        nodes.forEach { it.clip.init() }
    }

    private val keys = nodes.map { it.threshold }.toFloatArray()
    private var lastFiltered: Float = 0f
    private var lastTime: Float = 0f

    fun reset(query: EntityQuery, time: Float) {
        nodes.forEach { it.reset(query, time) }
        lastTime = time
        lastFiltered = factor(query)
    }

    fun update(animations: Map<String, Animation>, query: EntityQuery, node: Node, time: Float): TrsTransformF? {
        if (nodes.isEmpty()) return null
        // Сглаживание фактора
        val raw = factor(query)
        val filtered = if (smoothingTime > 0f && lastTime != 0f) {
            val dt = (time - lastTime).coerceAtLeast(0f)
            val alpha = (dt / smoothingTime).coerceIn(0f..1f)
            lastFiltered + (raw - lastFiltered) * alpha
        } else raw
        lastFiltered = filtered
        lastTime = time

        // Поиск узлов
        return when {
            filtered <= keys.first() || keys.size == 1 -> nodes.first().update(animations, query, node, time)
            filtered >= keys.last() -> nodes.last().update(animations, query, node, time)
            else -> {
                val idx = Arrays.binarySearch(keys, filtered).let { if (it >= 0) it else (-it - 2) }
                val prev = nodes[idx]
                val next = nodes[idx + 1]
                val local = filtered - prev.threshold
                val delta = next.threshold - prev.threshold
                val t = (local / delta).coerceIn(0f..1f)
                val first = prev.update(animations, query, node, time)
                val second = next.update(animations, query, node, time)
                first.mix(second, t)
            }
        }
    }
}

class BlendTreeBuilder {
    private val nodes = mutableListOf<BlendNode>()
    private var factor: String = "1f"
    private var smoothingTime: Float = 0f

    fun clip(
        name: String,
        threshold: Float,
        wrap: WrapMode = WrapMode.Loop,
        blend: BlendMode = BlendMode.Override,
        speed: String,
    ) {
        nodes += BlendNode(ClipNode(name, wrap, blend, speed), threshold)
    }

    /**
     * Устанавливает функцию фактора смешивания (например, скорость) и время сглаживания.
     * @param smoothingTime Время сглаживания в секундах (0 - без сглаживания)
     */
    fun factor(function: String, smoothingTime: Float = 0f) {
        this.factor = function
        this.smoothingTime = smoothingTime
    }

    fun build(): BlendTree = BlendTree(factor, nodes.sortedBy { it.threshold }, smoothingTime)
}

@Serializable
data class BlendNode(val clip: ClipNode, val threshold: Float) {
    fun update(animations: Map<String, Animation>, query: EntityQuery, node: Node, time: Float): TrsTransformF? {
        return clip.update(animations, query, node, time)
    }

    fun reset(query: EntityQuery, time: Float) {
        clip.reset(query, time)
    }

}

// Procedural

@Serializable
data class ProceduralNode(val functions: HashMap<String, ProceduralTransformer>) {
    @Transient
    val commandsDeferred = MolangCompilerScope.async {
        functions.forEach { (node, command) ->
            val nodeCommands = commands.computeIfAbsent(node) { ArrayList() }
            if (command.translation.isNotEmpty()) {
                Molang.compileVec3f(command.translation).let { exec ->
                    nodeCommands.add { model, query ->
                        model.animationPlayer.nodeModels.filter { it.name == node && it.mesh == null }
                            .forEach { it.transform.translation.set(exec(query)) }
                    }
                }
            }
            if (command.rotation.isNotEmpty()) {
                Molang.compileQuatF(command.rotation).let { exec ->
                    nodeCommands.add { model, query ->
                        model.animationPlayer.nodeModels.filter { it.name == node && it.mesh == null }
                            .forEach { it.transform.rotation.set(exec(query)) }
                    }
                }
            }
            if (command.scale.isNotEmpty()) {
                Molang.compileVec3f(command.scale).let { exec ->
                    nodeCommands.add { model, query ->
                        model.animationPlayer.nodeModels.filter { it.name == node && it.mesh == null }
                            .forEach { it.transform.scale.set(exec(query)) }
                    }
                }
            }
        }
    }

    @Transient
    val commands = HashMap<String, MutableList<(AnimatedModel, EntityQuery) -> Unit>>()

    suspend fun init() {
        commandsDeferred.await()
    }


    fun evaluate(model: AnimatedModel, query: EntityQuery) {
        commands.values.forEach { command -> command.forEach { it(model, query) } }
    }
}

class ModelContext(private val proceduralCommands: HashMap<String, ProceduralTransformer>) {
    fun setBoneRotation(node: String, rotation: String) {
        proceduralCommands.getOrPut(node) { ProceduralTransformer() }.rotation = rotation
    }

    fun setBoneTranslation(node: String, translation: String) {
        proceduralCommands.getOrPut(node) { ProceduralTransformer() }.translation = translation
    }

    fun setBoneScale(node: String, scale: String) {
        proceduralCommands.getOrPut(node) { ProceduralTransformer() }.scale = scale
    }
}

class ProceduralBuilder {
    private val proceduralCommands = HashMap<String, ProceduralTransformer>()

    fun onEvaluate(block: (context: ModelContext) -> Unit) {
        block(ModelContext(proceduralCommands))
    }

    fun build() = ProceduralNode(proceduralCommands)
}

@Serializable
class ProceduralTransformer {
    var translation = ""
    var rotation = ""
    var scale = ""
}

@Serializable
data class ClipNode(
    val name: String,
    val wrap: WrapMode,
    val blend: BlendMode,
    private val function: String,
) {
    @Transient
    private lateinit var speed: (EntityQuery) -> Float

    @Transient
    private val speedDeferred = MolangCompilerScope.async {
        Molang.compileFloat(function)
    }

    suspend fun init() {
        speed = speedDeferred.await()
    }

    private var pausedAnimTime: Float = 0f
    private var startTime = 0f
    private var oldSpeed = 0f


    fun reset(query: EntityQuery, time: Float) {
        pausedAnimTime = 0f
        startTime = time
        oldSpeed = speed(query)
    }

    fun update(animations: Map<String, Animation>, query: EntityQuery, node: Node, time: Float): TrsTransformF? {
        val newSpeed = speed(query).coerceIn(-3f, 3f)

        if (newSpeed != oldSpeed) {
            val currentRaw = (time - startTime) * oldSpeed

            if (newSpeed == 0f) {
                pausedAnimTime = currentRaw
            } else {
                startTime = time - (currentRaw / newSpeed)
            }

            oldSpeed = newSpeed
        }

        val rawTime = if (oldSpeed == 0f) pausedAnimTime
        else (time - startTime) * oldSpeed

        val animation = animations[name] ?: return null

        when (wrap) {
            WrapMode.Once -> {
                if (oldSpeed > 0 && rawTime > animation.maxTime) return null
                if (oldSpeed < 0 && rawTime < 0f) return null
            }

            WrapMode.Reverse -> {
                if (oldSpeed > 0 && rawTime < 0f) return null
                if (oldSpeed < 0 && rawTime > animation.maxTime) return null
            }

            else -> {}
        }

        val sampleTime = wrap.wrapTime(animation, rawTime)
        animation.computeWeights(node, sampleTime)?.let { weights ->
            node.mesh?.weights?.let { nodeWeights ->
                weights.copyInto(nodeWeights)
            }
        }
        return animation.compute(node, sampleTime)
    }

}

@Serializable
data class DeltaNode(val targetPose: String)

// Transition builder

class TransitionBuilder(val from: State, val to: State) {
    internal var condition: String = "false"
    internal var duration: Float = 0.2f
    internal var transitionClip: ClipNode? = null
    internal var deltaClip: DeltaNode? = null

    fun condition(block: String) {
        condition = block
    }

    fun duration(sec: Float) {
        duration = sec
    }

    fun clip(name: String, wrap: WrapMode = WrapMode.Once, speed: String = "1f") {
        transitionClip = ClipNode(name, wrap, BlendMode.Override, speed)
    }

    fun deltaClip(targetPose: String) {
        deltaClip = DeltaNode(targetPose)
    }

    fun build() = Transition(from, to, condition, duration, transitionClip, deltaClip)
}