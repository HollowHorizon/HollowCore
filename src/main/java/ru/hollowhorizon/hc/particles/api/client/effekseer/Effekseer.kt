package ru.hollowhorizon.hc.particles.api.client.effekseer

import Effekseer.swig.EffekseerBackendCore

open class Effekseer protected constructor(private val impl: EffekseerBackendCore) :
    SafeFinalized<EffekseerBackendCore>(impl, EffekseerBackendCore::delete) {
    constructor() : this(EffekseerBackendCore())

    override fun close() {
        impl.delete()
    }

    companion object {
        @JvmStatic
        fun init(): Boolean {
            return EffekseerBackendCore.InitializeWithOpenGL()
        }

        @JvmStatic
        fun terminate() {
            EffekseerBackendCore.Terminate()
        }

        @JvmStatic
        val deviceType: DeviceType
            get() = DeviceType.fromNativeOrdinal(EffekseerBackendCore.GetDevice().swigValue())
    }
}
