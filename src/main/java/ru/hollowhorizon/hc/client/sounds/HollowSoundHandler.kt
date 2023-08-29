package ru.hollowhorizon.hc.client.sounds

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import ru.hollowhorizon.hc.HollowCore
import java.io.InputStream

object HollowSoundHandler {
    val MODS = hashSetOf<String>()

    @JvmStatic
    fun createJson(manager: ResourceManager, modId: String): Resource.IoSupplier<InputStream> {
        val soundsJson = JsonObject()

        manager.listResources("sounds") {
            it.namespace == modId && it.path.endsWith(".ogg")
        }.map {
            it.key
        }.forEach { location ->
            val soundName = location.path.substringAfter("sounds/").substringBeforeLast(".")
            soundsJson.add(soundName, JsonObject().apply {
                addProperty("category", "master")
                add(
                    "sounds",
                    JsonArray().apply {
                        add(
                            location.namespace + ":" + location.path.substringAfter("sounds/").substringBeforeLast(".")
                        )
                    })
            })
        }

        HollowCore.LOGGER.info("Creating sounds.json for $modId: $soundsJson")
        return Resource.IoSupplier { soundsJson.toString().byteInputStream() }
    }
}