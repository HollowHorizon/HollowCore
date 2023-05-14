package ru.hollowhorizon.hc.client.gltf

import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimation

interface IAnimatedEntity {
    var renderedGltfModel: RenderedGltfModel?
    var animationList: List<GLTFAnimation>
}