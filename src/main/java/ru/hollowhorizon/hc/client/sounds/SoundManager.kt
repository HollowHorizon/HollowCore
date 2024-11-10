package ru.hollowhorizon.hc.client.sounds

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.audio.Wave
import ru.hollowhorizon.hc.client.audio.formats.Mp3Format
import ru.hollowhorizon.hc.client.audio.formats.OggFormat
import ru.hollowhorizon.hc.client.audio.formats.WavFormat

object SoundManager : ResourceManagerReloadListener {
    private val SUPPORTED_FORMATS = arrayOf("ogg", "mp3", "wav")
    val SOUNDS = hashMapOf<ResourceLocation, Wave>()

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        SOUNDS.clear()
        resourceManager.listResources("audio") { it.path.substringAfterLast('.') in SUPPORTED_FORMATS }
            .map { (location, resource) ->
                try {
                    val stream = resource.open()
                    when (location.path.substringAfterLast('.')) {
                        "mp3" -> Mp3Format.read(stream)
                        "wav" -> WavFormat.read(stream)
                        "ogg" -> OggFormat.read(stream)
                        else -> error("Unsupported audio format $location")
                    }
                } catch (e: Exception) {
                    HollowCore.LOGGER.warn("Error while loading $location", e)
                }
            }
    }
}