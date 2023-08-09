package ru.hollowhorizon.hc.client.sounds

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ReloadableResourceManager
import net.minecraft.sounds.SoundEvent
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.registry.Registries

object HollowSoundHandler {
    val MODS = hashSetOf<String>()

    @JvmStatic
    fun reloadResources(manager: ReloadableResourceManager) {
        for (modId in MODS) {
            val soundsJson = JsonObject()

            manager.listResources("sounds") {
                it.path.endsWith(".ogg")
            }.filter {
                it.key.namespace == modId
            }.map {
                it.key
            }.forEach { location ->
                val soundName = location.path.substringAfterLast('/').substringBeforeLast('.')
                val soundEvent = SoundEvent(ResourceLocation(modId, soundName))
                Registries.getRegistry(ForgeRegistries.SOUND_EVENTS, modId).register(soundName) { soundEvent }
                soundsJson.add(soundName, JsonObject().apply {
                    addProperty("category", "master")
                    add("sounds", JsonArray().apply { add(location.namespace+":"+location.path.substringAfter("sounds/").substringBeforeLast(".")) })
                })
            }

            HollowPack.genSounds[modId] = soundsJson
        }
    }
}