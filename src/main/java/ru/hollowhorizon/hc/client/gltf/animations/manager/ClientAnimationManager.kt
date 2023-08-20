package ru.hollowhorizon.hc.client.gltf.animations.manager

import com.modularmods.mcgltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.animations.AnimationException
import ru.hollowhorizon.hc.client.gltf.animations.AnimationLayer
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.PlayType

class ClientAnimationManager(model: RenderedGltfModel) : AnimationManager(model), IAnimationManager {
    override fun startAnimation(name: String, priority: Float, playType: PlayType, speed: Float) {
        this.addLayer(
            AnimationLayer(
                animationCache[name] ?: throw AnimationException("Animation \"$name\" not found!"),
                priority,
                playType
            )
        )
    }

    override fun stopAnimation(name: String) {
        this.removeAnimation(name)
    }
}