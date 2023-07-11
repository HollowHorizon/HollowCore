package ru.hollowhorizon.hc.client.gltf

import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager

interface IAnimatedEntity {
    var renderedGltfModel: RenderedGltfModel?
    var animationManager: AnimationManager?
}