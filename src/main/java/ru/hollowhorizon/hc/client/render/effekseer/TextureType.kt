package ru.hollowhorizon.hc.client.render.effekseer

import Effekseer.swig.EffekseerTextureType

@Suppress("unused")
enum class TextureType(val impl: EffekseerTextureType) {
    COLOR(EffekseerTextureType.Color),
    NORMAL(EffekseerTextureType.Normal),
    DISTORTION(EffekseerTextureType.Distortion);

    override fun toString() = impl.toString()
    val nativeOrdinal get() = impl.swigValue()

    companion object {
        fun fromNativeOrdinal(ord: Int) = values().firstOrNull { it.nativeOrdinal == ord }
    }
}