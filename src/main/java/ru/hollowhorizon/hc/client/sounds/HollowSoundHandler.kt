package ru.hollowhorizon.hc.client.sounds

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import ru.hollowhorizon.hc.HollowCore
import java.io.InputStream

object HollowSoundHandler {
    val MODS = hashSetOf<String>()

    @JvmStatic
    fun createJson(manager: ResourceManager, modId: String): InputStream {
        val soundsJson = JsonObject()

        manager.listResources("sounds") {
            true //по какой-то причине фильтр на 1.18.2 работает криво, поэтому проверка ниже
        }.forEach { location ->
            if(location.namespace != modId || !location.path.endsWith(".ogg")) return@forEach
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
        return soundsJson.toString().byteInputStream()
    }
}