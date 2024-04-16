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

package ru.hollowhorizon.hc.client.render.effekseer

import net.minecraft.resources.ResourceLocation
import java.io.Closeable
import java.util.*
import java.util.random.RandomGenerator
import java.util.stream.Stream
import kotlin.math.abs

class EffectDefinition : Closeable {


    @JvmOverloads
    fun play(
        type: ParticleEmitter.Type = ParticleEmitter.Type.WORLD,
        emitterName: ResourceLocation? = null,
    ): ParticleEmitter {
        val emitter = getManager(type).createParticle(effect, type)

        if (emitterName == null) {
            oneShotEmitters[type]?.add(emitter)
        } else {
            val old = namedEmitters[type]?.put(emitterName, emitter)
            old?.stop()
        }
        return emitter
    }

    fun getManager(type: ParticleEmitter.Type): EffekseerManager {
        return managers[type] ?: throw IllegalStateException("No manager for type $type")
    }

    fun emitters(): Stream<ParticleEmitter> {
        return emitterContainers().flatMap { it.stream() }
    }

    fun emitters(type: ParticleEmitter.Type): Stream<ParticleEmitter> {
        return emitterContainers(type).flatMap { obj: Collection<ParticleEmitter> -> obj.stream() }
    }

    fun emitterContainers(): Stream<MutableCollection<ParticleEmitter>> {
        return Stream.concat(
            oneShotEmitters.values.stream(),
            namedEmitters.values.stream().map { it.values }
        )
    }

    fun emitterContainers(type: ParticleEmitter.Type): Stream<Collection<ParticleEmitter>> {
        val oneshot = oneShotEmitters[type]
        val named = namedEmitters[type]!!.values
        return Stream.of(oneshot, named)
    }

    fun setEffect(effect: EffekseerEffect): EffectDefinition? {
        if (this::effect.isInitialized) {
            if (this.effect == effect) return null
            emitters().forEach { it.stop() }
            managers().forEach { it.close() }
            this.effect.close()
            managers.clear()
        }
        this.effect = effect
        initManager()
        return this
    }

    fun managers(): Stream<EffekseerManager> {
        return managers.values.stream()
    }

    private lateinit var effect: EffekseerEffect

    private val managers = EnumMap<ParticleEmitter.Type, EffekseerManager>(ParticleEmitter.Type::class.java)
    private val oneShotEmitters =
        EnumMap<ParticleEmitter.Type, MutableSet<ParticleEmitter>>(ParticleEmitter.Type::class.java)
    private val namedEmitters =
        EnumMap<ParticleEmitter.Type, MutableMap<ResourceLocation?, ParticleEmitter>>(ParticleEmitter.Type::class.java)
    private val magicLoadBalancer = (abs((RNG.nextInt() ushr 2).toDouble()) % GC_DELAY).toInt()
    private var gcTicks = 0

    init {
        for (type in ParticleEmitter.Type.values()) {
            oneShotEmitters[type] = LinkedHashSet()
            namedEmitters[type] = LinkedHashMap()
        }
    }

    fun draw(
        type: ParticleEmitter.Type,
        w: Int,
        h: Int,
        camera: FloatArray,
        projection: FloatArray,
        deltaFrames: Float,
        partialTicks: Float,
    ) {
        val manager = managers[type] ?: return
        manager.setViewport(w, h)
        manager.setCameraMatrix(camera)
        manager.setProjectionMatrix(projection)
        manager.update(deltaFrames)
        emitters(type).forEach { it.runPreDrawCallbacks(partialTicks) }
        manager.draw()

        if (type == ParticleEmitter.Type.WORLD) {
            gcTicks = (gcTicks + 1) % GC_DELAY
            if (gcTicks == magicLoadBalancer) {
                emitterContainers().forEach { container -> container.removeIf { !it.exists() } }
            }
        }
    }

    private fun initManager() {
        for (type in ParticleEmitter.Type.values()) {
            val old = managers.put(type, EffekseerManager())
            Optional.ofNullable(old).ifPresent { obj: EffekseerManager -> obj.close() }
        }
        val worldManager = managers[ParticleEmitter.Type.WORLD]!!
        val mainHandManager = managers[ParticleEmitter.Type.FIRST_PERSON_MAINHAND]!!
        val offHandManager = managers[ParticleEmitter.Type.FIRST_PERSON_OFFHAND]!!
        check(worldManager.init(9000)) { "Failed to initialize EffekseerManager" }
        check(mainHandManager.init(500)) { "Failed to initialize (fpv mainhand) EffekseerManager" }
        check(offHandManager.init(500)) { "Failed to initialize (fpv offhand) EffekseerManager" }
        worldManager.setupWorkerThreads(2)
        mainHandManager.setupWorkerThreads(1)
        offHandManager.setupWorkerThreads(1)
    }

    override fun close() {
        managers.values.forEach { it.close() }
        effect.close()
    }

    companion object {
        private val RNG: RandomGenerator = Random()
        private const val GC_DELAY = 20
    }
}
