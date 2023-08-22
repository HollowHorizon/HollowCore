package ru.hollowhorizon.hc.client.gltf.animations.manager

import com.modularmods.mcgltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.Transform
import ru.hollowhorizon.hc.client.gltf.animations.*
import ru.hollowhorizon.hc.client.utils.rl

class ClientModelManager(model: RenderedGltfModel) : GLTFAnimationManager(model), IModelManager {
    override var transform = Transform()

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

    override fun setDefaultAnimations(animations: Map<AnimationType, String>) {
        this.templates.clear()
        this.templates.putAll(animations)
    }
}