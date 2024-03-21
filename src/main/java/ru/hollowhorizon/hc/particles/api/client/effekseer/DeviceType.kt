package ru.hollowhorizon.hc.particles.api.client.effekseer

import Effekseer.swig.EffekseerCoreDeviceType

enum class DeviceType(val impl: EffekseerCoreDeviceType) {
    UNKNOWN(EffekseerCoreDeviceType.Unknown),
    OPENGL(EffekseerCoreDeviceType.OpenGL);

    val nativeOrdinal get() = impl.swigValue()

    companion object {
        fun fromNativeOrdinal(id: Int) =
            values().find { it.nativeOrdinal == id } ?: throw IllegalArgumentException("Unknown DeviceType = $id")
    }
}
