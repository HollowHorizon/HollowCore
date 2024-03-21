package ru.hollowhorizon.hc.particles.client

import ru.hollowhorizon.hc.particles.EffekseerParticles
import ru.hollowhorizon.hc.particles.api.common.ParticleEmitterInfo
import ru.hollowhorizon.hc.particles.client.installer.JarExtractor.extract
import ru.hollowhorizon.hc.particles.client.installer.NativePlatform.Companion.current
import ru.hollowhorizon.hc.particles.client.loader.EffekAssetLoader
import net.minecraft.client.Minecraft
import net.minecraft.world.level.Level
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.HollowCore
import thedarkcolour.kotlinforforge.KotlinModLoadingContext.Companion.get

object EffekseerParticlesClient {
    private const val DLL_NAME = "EffekseerNativeForJava"

    @JvmStatic
    fun init() {
        installNativeLibrary()
        get().getKEventBus().addListener(::onRegisterReloadListener)
    }

    private fun onRegisterReloadListener(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(EffekAssetLoader())
    }


    private fun installNativeLibrary() {
        val platform = current()

        val dll = platform.getNativeInstallPath(DLL_NAME)
        if (!dll.isFile) {
            EffekseerParticles.LOGGER.info("Installing Effekseer native library at " + dll.canonicalPath)
            val resource = "natives/${platform.formatFileName(DLL_NAME)}"
            extract(resource, dll)
        } else {
            EffekseerParticles.LOGGER.info("Loading Effekseer native library at " + dll.canonicalPath)
        }
        System.load(dll.canonicalPath)
    }

    fun addParticle(level: Level, info: ParticleEmitterInfo) {
        val player = Minecraft.getInstance().player
        if (player != null && player.level !== level) {
            return
        }
        info.spawnInWorld(level, player)
    }
}
