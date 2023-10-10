package ru.hollowhorizon.hc.client.models.gltf.manager

import ru.hollowhorizon.hc.client.gltf.animations.GLTFAnimationManager
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationException
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationLayer
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayType
import ru.hollowhorizon.hc.client.models.gltf.manager.IModelManager

class ClientModelManager(model: GltfModel) : GLTFAnimationManager(model), IModelManager {

    override fun startAnimation(name: String, priority: Float, playType: PlayType, speed: Float) {
        if (playType == PlayType.ONCE) {
            this.setSmoothAnimation(name, true)
        } else this.addLayer(
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