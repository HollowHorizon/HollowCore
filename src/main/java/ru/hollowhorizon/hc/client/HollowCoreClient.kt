package ru.hollowhorizon.hc.client

import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.client.render.effekseer.EffekseerNatives
import ru.hollowhorizon.hc.client.render.effekseer.loader.EffekAssetLoader
import thedarkcolour.kotlinforforge.KotlinModLoadingContext

object HollowCoreClient {
    init {
        EffekseerNatives.install()
        KotlinModLoadingContext.get().getKEventBus().addListener(::onRegisterReloadListener)
    }

    private fun onRegisterReloadListener(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(EffekAssetLoader())
    }
}