package ru.hollowhorizon.hc.client.sounds

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.IReloadableResourceManager
import net.minecraft.util.ResourceLocation
import net.minecraft.util.SoundEvent
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.common.registry.Registries

object HollowSoundHandler {
    val MODS = hashSetOf<String>()

    @JvmStatic
    fun reloadResources(manager: IReloadableResourceManager) {
        for (modId in MODS) {
            val soundsJson = JsonObject()

            manager.listResources("sounds") {
                it.endsWith(".ogg")
            }.filter {
                it.namespace == modId
            }.map {
                manager.getResource(it)
            }.forEach { sound ->
                val soundName = sound.location.path.substringAfterLast('/').substringBeforeLast('.')
                val soundEvent = SoundEvent(ResourceLocation(modId, soundName))
                Registries.getRegistry(ForgeRegistries.SOUND_EVENTS, modId).register(soundName) { soundEvent }
                soundsJson.add(soundName, JsonObject().apply {
                    addProperty("category", "master")
                    add("sounds", JsonArray().apply { add(sound.location.namespace+":"+sound.location.path.substringAfter("sounds/").substringBeforeLast(".")) })
                })
            }

            HollowPack.genSounds[modId] = soundsJson
        }
    }
}