package ru.hollowhorizon.hc.client.gltf.animation

import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel

class GLTFAnimation(val name: String, val channel: List<InterpolatedChannel>) {
    fun update(time: Float, loop: Boolean): Boolean {
        val deltaTime = time / 30f
        var isEnd = false
        channel.parallelStream().forEach { channel ->
            val calcTime = deltaTime % channel.keys[channel.keys.size - 1];
            channel.update(calcTime)
            if (!loop && calcTime >= channel.keys[channel.keys.size - 1] - 0.1) isEnd = true
        }
        return isEnd
    }
}

@Serializable
class GLTFAnimationContainer(val name: String, val loop: Boolean = false) {
    private var ticker = 0f

    fun tick(partialTick: Float): Float {
        ticker += partialTick
        return ticker
    }

    fun reset() {
        ticker = 0f
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