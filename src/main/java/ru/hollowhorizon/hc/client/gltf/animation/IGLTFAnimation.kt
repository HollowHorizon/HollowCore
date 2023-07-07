package ru.hollowhorizon.hc.client.gltf.animation

interface IGLTFAnimation {
    fun update(time: Float, playType: PlayType): Boolean
}