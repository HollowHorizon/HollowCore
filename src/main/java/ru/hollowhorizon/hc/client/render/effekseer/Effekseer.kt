package ru.hollowhorizon.hc.client.render.effekseer

import Effekseer.swig.EffekseerBackendCore

open class Effekseer(private val impl: EffekseerBackendCore = EffekseerBackendCore()) :
    SafeFinalized<EffekseerBackendCore>(impl, EffekseerBackendCore::delete) {

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
    }
}
