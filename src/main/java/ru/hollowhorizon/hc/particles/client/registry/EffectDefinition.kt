package ru.hollowhorizon.hc.particles.client.registry

import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.particles.api.client.effekseer.EffekseerEffect
import ru.hollowhorizon.hc.particles.api.client.effekseer.EffekseerManager
import ru.hollowhorizon.hc.particles.api.client.effekseer.ParticleEmitter
import java.io.Closeable
import java.util.*
import java.util.random.RandomGenerator
import java.util.stream.Stream
import kotlin.math.abs

class EffectDefinition : Closeable {

    @JvmOverloads
    fun play(type: ParticleEmitter.Type = ParticleEmitter.Type.WORLD): ParticleEmitter {
        val emitter = getManager(type).createParticle(effect, type)
        oneShotEmitters[type]?.add(emitter)
        return emitter
    }

    @JvmOverloads
    fun play(type: ParticleEmitter.Type = ParticleEmitter.Type.WORLD, emitterName: ResourceLocation): ParticleEmitter {
        val emitter = getManager(type).createParticle(effect, type)
        val old = namedEmitters[type]?.put(emitterName, emitter)
        old?.stop()
        return emitter
    }

    fun getManager(type: ParticleEmitter.Type): EffekseerManager {
        return managers[type] ?: throw IllegalStateException("No manager for type $type")
    }

    fun emitters(): Stream<ParticleEmitter> {
        return emitterContainers().flatMap { it.stream() }
    }

    fun emitters(type: ParticleEmitter.Type?): Stream<ParticleEmitter> {
        return emitterContainers(type).flatMap { obj: Collection<ParticleEmitter> -> obj.stream() }
    }

    fun emitterContainers(): Stream<MutableCollection<ParticleEmitter>> {
        return Stream.concat(
            oneShotEmitters.values.stream(),
            namedEmitters.values.stream()
                .map { obj: Map<ResourceLocation?, ParticleEmitter> -> obj.values.toMutableList() }
        )
    }

    fun emitterContainers(type: ParticleEmitter.Type?): Stream<Collection<ParticleEmitter>> {
        val oneshot = Objects.requireNonNull<Set<ParticleEmitter>>(oneShotEmitters[type])
        val named = Objects.requireNonNull<Map<ResourceLocation?, ParticleEmitter>>(
            namedEmitters[type]
        ).values
        return Stream.of(oneshot, named)
    }

    fun setEffect(effect: EffekseerEffect): EffectDefinition? {
        if (this::effect.isInitialized && this.effect === effect) return null

        if (this::effect.isInitialized) {
            emitters().forEach { obj: ParticleEmitter -> obj.stop() }
            managers().forEach { obj: EffekseerManager -> obj.close() }
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

    lateinit var effect: EffekseerEffect
        private set
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
        camera: FloatArray?,
        projection: FloatArray?,
        deltaFrames: Float,
        partialTicks: Float,
    ) {
        val manager = Objects.requireNonNull(managers[type])
        manager!!.setViewport(w, h)
        manager.setCameraMatrix(camera!!)
        manager.setProjectionMatrix(projection!!)
        manager.update(deltaFrames)
        emitters(type).forEach { emitter: ParticleEmitter -> emitter.runPreDrawCallbacks(partialTicks) }
        manager.draw()

        if (type == ParticleEmitter.Type.WORLD) {
            gcTicks = (gcTicks + 1) % GC_DELAY
            if (gcTicks == magicLoadBalancer) {
                emitterContainers().forEach { container: MutableCollection<ParticleEmitter> -> container.removeIf { emitter: ParticleEmitter -> !emitter.exists() } }
            }
        }
    }

    private fun initManager() {
        for (type in ParticleEmitter.Type.values()) {
            val old = managers.put(type, EffekseerManager())
            Optional.ofNullable(old).ifPresent { obj: EffekseerManager -> obj.close() }
        }
        val worldManager = managers[ParticleEmitter.Type.WORLD]!!
        val fpvMhManager = managers[ParticleEmitter.Type.FIRST_PERSON_MAINHAND]!!
        val fpvOhManager = managers[ParticleEmitter.Type.FIRST_PERSON_OFFHAND]!!
        check(worldManager.init(9000)) { "Failed to initialize EffekseerManager" }
        check(fpvMhManager.init(500)) { "Failed to initialize (fpv mainhand) EffekseerManager" }
        check(fpvOhManager.init(500)) { "Failed to initialize (fpv offhand) EffekseerManager" }
        worldManager.setupWorkerThreads(2)
        fpvMhManager.setupWorkerThreads(1)
        fpvOhManager.setupWorkerThreads(1)
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
