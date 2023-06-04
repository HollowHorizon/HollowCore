package ru.hollowhorizon.hc.client.gltf.animation

import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel

open class GLTFAnimation(val name: String, val channel: List<InterpolatedChannel>) : IGLTFAnimation {
    var switch = false

    override fun update(time: Float, playType: PlayType): Boolean {
        val deltaTime = time / 30f
        var isEnd = false

        when (playType) {
            PlayType.ONCE -> {
                channel.parallelStream().forEach { channel ->
                    val calcTime = deltaTime % channel.keys[channel.keys.size - 1]
                    channel.update(calcTime)
                    if (calcTime >= channel.keys[channel.keys.size - 1] - 0.1) isEnd = true
                }
                if (isEnd) return true
            }

            PlayType.LOOPED -> {
                channel.parallelStream().forEach { channel ->
                    val calcTime = deltaTime % channel.keys[channel.keys.size - 1]
                    channel.update(calcTime)
                }
            }

            PlayType.PING_PONG -> {
                channel.parallelStream().forEach { channel ->
                    val calcTime = if (!switch) deltaTime % channel.keys[channel.keys.size - 1]
                    else channel.keys[channel.keys.size - 1] - deltaTime % channel.keys[channel.keys.size - 1]
                    channel.update(calcTime)
                    if (calcTime >= channel.keys[channel.keys.size - 1] - 0.1) switch = !switch
                }
            }

            PlayType.LAST_FRAME -> {
                channel.parallelStream().forEach { channel ->
                    val calcTime = if (!switch) deltaTime % channel.keys[channel.keys.size - 1]
                    else channel.keys[channel.keys.size - 1]
                    channel.update(calcTime)
                    if (calcTime >= channel.keys[channel.keys.size - 1] - 0.1) switch = true
                }
            }
        }

        return false
    }
}

@Serializable
class GLTFAnimationContainer(val name: String, val playType: PlayType) {
    private var ticker = 0f
    private var partialTickO = 0f

    fun tick(partialTick: Float): Float {
        if (partialTick >= partialTickO) ticker++
        partialTickO = partialTick
        return ticker + partialTick
    }

    fun reset() {
        ticker = 0f
        partialTickO = 0f
    }
}

fun RenderedGltfModel.loadAnimation(name: String): GLTFAnimation {
    val animation = this.gltfModel.animationModels.find { it.name == name }
        ?: throw IllegalArgumentException("Animation $name not found").apply { HollowCore.LOGGER.error(message) }
    val channels = GltfAnimationCreator.createGltfAnimation(animation)
    return GLTFAnimation(name, channels)
}

fun RenderedGltfModel.loadAnimations(): List<GLTFAnimation> {
    return this.gltfModel.animationModels.mapNotNull { if (it.name != null) loadAnimation(it.name) else null }
}