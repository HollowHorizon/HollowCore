package ru.hollowhorizon.hc.client.gltf.animation

interface IGLTFAnimation {
    fun update(time: Float, loop: PlayType): Boolean
}