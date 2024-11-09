package ru.hollowhorizon.hc.client.particles

import dev.folomeev.kotgl.matrix.vectors.Vec3
import dev.folomeev.kotgl.matrix.vectors.mutables.minus
import dev.folomeev.kotgl.matrix.vectors.mutables.plus
import dev.folomeev.kotgl.matrix.vectors.vecZero
import ru.hollowhorizon.hc.client.audio.SoundBuffer
import ru.hollowhorizon.hc.client.audio.SoundPlayer
import ru.hollowhorizon.hc.client.molang.MolangContext
import ru.hollowhorizon.hc.client.molang.MolangQuery
import ru.hollowhorizon.hc.client.molang.MolangQueryEntity
import ru.hollowhorizon.hc.client.molang.VariablesMap
import ru.hollowhorizon.hc.client.particles.file.BedrockParticleFile
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.client.utils.math.rotateBy
import ru.hollowhorizon.hc.client.utils.math.rotateSelfBy

class ParticleEmitter(
    val system: ParticleSystem,
    val effect: ParticleEffect,
    val sourceEntity: MolangQueryEntity,
    var position: Vec3,
    var rotation: Quaternion,
    var velocity: Vec3,
    val transform: Transform?,
    val offset: Vec3? = null,
) {
    val particles = arrayListOf<BedrockParticle>()
    private val components = effect.components
    private val curveVariables = CurveVariables({ context }, effect.curves)
    private val variables = VariablesMap().fallbackBackTo(curveVariables)
    val context: MolangContext = MolangContext(MolangQuery.Empty, variables)

    init {
        variables["entity_scale"] = 1f
        components.emitterInitialization?.creationExpression?.eval(context)
    }

    private var firedCreationEvents = false
    private var firedExpirationEvents = false

    private var age: Float by variables.getOrPut("emitter_age", 0f)
    private var activeTime: Float by variables.getOrPut("emitter_lifetime", 0f)
    private var sleepTime: Float = 0f
    private var maxParticles: Float = 0f
    private var cooldown: Float = 0f
    private var nextTimelineEvent: Map.Entry<Float, List<String>>? = null

    fun startLoop(timeSince: Float) {
        for (i in 1..4) variables["emitter_random_$i"] = system.random.nextFloat()

        age = 0f
        activeTime = components.emitterLifetimeLooping?.activeTime?.eval(context)
            ?: components.emitterLifetimeOnce?.activeTime?.eval(context) ?: Float.POSITIVE_INFINITY
        sleepTime = components.emitterLifetimeLooping?.sleepTime?.eval(context) ?: 0f

        repeat(components.emitterRateInstant?.numParticles?.eval(context)?.toInt() ?: 0) {
            emit(timeSince)
        }

        maxParticles = components.emitterRateSteady?.maxParticles?.eval(context) ?: Float.POSITIVE_INFINITY

        cooldown = components.emitterRateSteady?.spawnRate?.eval(context)?.let { 1 / it }
            ?: Float.POSITIVE_INFINITY

        if (!firedCreationEvents) {
            firedCreationEvents = true
            fire(timeSince, components.emitterLifetimeEvents.creationEvents, null)
        }

        nextTimelineEvent = components.emitterLifetimeEvents.timeline.lowestEntry()
    }

    fun skip(dt: Float) {
        age += dt
    }

    fun update(dt: Float): Boolean {
        val alive = doUpdate(dt)

        if (!alive && !firedExpirationEvents) {
            firedExpirationEvents = true
            fire(0f, components.emitterLifetimeEvents.expirationEvents, null)
        }

        val expiredParticles = particles.filter { !it.update(dt) }.onEach(::onRemove)

        particles.removeAll(expiredParticles.toSet())

        return alive
    }

    private fun doUpdate(dt: Float): Boolean {
        age += dt

        curveVariables.update()
        components.emitterInitialization?.perUpdateExpression?.eval(context)

        transform?.let {
            position = it.position.plus(offset?.rotateBy(rotation) ?: vecZero())
            rotation = it.rotation
            velocity = it.velocity

            if (!transform.isValid) return false
        }

        fireTimelineEvents()

        components.emitterLifetimeExpression?.let { config ->
            if (config.expirationExpression.eval(context) != 0f) return false
            if (config.activationExpression.eval(context) == 0f) return true
        }

        if (age > activeTime) {
            if (components.emitterLifetimeOnce != null) return false

            val loopTime = age - activeTime - sleepTime
            if (loopTime < 0) return true
            startLoop(loopTime)
        }

        cooldown -= dt
        while (cooldown < 0) {
            if (particles.size < maxParticles) {
                val emitTime = -cooldown
                age -= emitTime
                emit(emitTime)
                cooldown += components.emitterRateSteady?.spawnRate?.eval(context)?.let { 1 / it }
                    ?: Float.POSITIVE_INFINITY
                age += emitTime
            } else {
                cooldown = 0f
            }
        }

        return true
    }

    private fun emit(dt: Float, velocity: Boolean = false) {
        val localSpace = if (components.emitterLocalSpace?.position == true) transform else null

        val particle = BedrockParticle(this, localSpace)

        particle.emit(velocity)

        addParticle(particle)

        particle.update(dt)
    }

    private fun addParticle(particle: BedrockParticle) {
        particles.add(particle)

        val effect = particle.emitter.effect
        val renderPass = effect.renderPass ?: return
        if (effect.components.particleAppearanceBillboard == null) return

        system.billboardRenderPasses.getOrPut(renderPass, ::mutableSetOf).add(particle)
    }

    fun onRemove(particle: BedrockParticle) {
        val effect = particle.emitter.effect
        val renderPass = effect.renderPass ?: return
        if (effect.components.particleAppearanceBillboard == null) return

        val renderPassSet = system.billboardRenderPasses.getValue(renderPass)
        renderPassSet.remove(particle)
        if (renderPassSet.isNotEmpty()) return

        system.billboardRenderPasses.remove(renderPass)
    }

    private fun fireTimelineEvents() {
        while (true) {
            val (time, events) = nextTimelineEvent ?: return
            val timeSinceEvent = age - time
            if (timeSinceEvent < 0) return

            fire(timeSinceEvent, events, null)

            nextTimelineEvent = components.emitterLifetimeEvents.timeline.higherEntry(time)
        }
    }

    fun fire(timeSince: Float, events: List<String>, particle: BedrockParticle?) =
        events.forEach { fire(timeSince, it, particle) }

    fun fire(timeSince: Float, eventName: String, particle: BedrockParticle?) {
        val event = effect.events[eventName] ?: return
        fire(timeSince, event, particle)
    }

    fun fire(timeSince: Float, event: BedrockParticleFile.Event, particle: BedrockParticle?) {
        event.sequence?.forEach { fire(timeSince, it, particle) }

        event.randomize?.let { options ->
            val weights = options.sumOf { it.weight.toDouble() }
            var choice = system.random.nextFloat() * weights
            for (option in options) {
                choice -= option.weight
                if (choice <= 0) {
                    fire(timeSince, option.value, particle)
                    break
                }
            }
        }

        event.expression?.let { expr ->
            age -= timeSince
            expr.eval(context)
            age += timeSince
        }

        event.particle?.let { config ->
            val targetEffect = effect.referencedEffects[config.effect] ?: return@let

            val targetEmitter = if (config.type.isBound && transform != null) {
                ParticleEmitter(
                    system,
                    targetEffect,
                    sourceEntity,
                    particle?.globalPosition ?: position,
                    rotation,
                    particle?.globalVelocity ?: velocity,
                    transform,
                    particle?.globalPosition?.minus(transform.position)?.rotateSelfBy(transform.rotation.invert())
                        ?: offset,
                )
            } else {
                ParticleEmitter(
                    system,
                    targetEffect,
                    sourceEntity,
                    particle?.globalPosition ?: position,
                    Quaternion.Identity,
                    particle?.globalVelocity ?: velocity,
                    null,
                    null,
                )
            }
            config.preEffectExpression.eval(targetEmitter.context)
            if (config.type.isParticle) {
                targetEmitter.emit(timeSince, config.type.inheritVelocity)
            } else {
                system.emitters.add(targetEmitter)
                targetEmitter.startLoop(timeSince)
                targetEmitter.update(timeSince)
            }
        }

        event.sound?.let { config ->
            val targetSound = effect.referencedSounds[config.eventName] ?: return@let

            SoundPlayer(SoundBuffer(targetSound)).apply {
                setPosition(
                    transform?.position?.x ?: 0f,
                    transform?.position?.y ?: 0f,
                    transform?.position?.z ?: 0f
                )
                setRelative(transform == null)
            }.play()
        }
    }
}