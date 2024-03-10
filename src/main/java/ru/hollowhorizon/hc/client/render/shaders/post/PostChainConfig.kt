package ru.hollowhorizon.hc.client.render.shaders.post

import kotlinx.serialization.Serializable

@Serializable
class PostChainConfig {
    val floatUniforms = HashMap<String, Float>()
    val intUniforms = HashMap<String, Int>()

    operator fun String.invoke(value: Float) {

    }

    operator fun String.invoke(value: Int) {

    }
}