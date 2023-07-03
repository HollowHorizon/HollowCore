package ru.hollowhorizon.hc.client.gltf

import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimation
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager

interface IAnimatedEntity {
    var renderedGltfModel: RenderedGltfModel?
    var animationList: List<GLTFAnimation>
    var animationManager: AnimationManager?
}