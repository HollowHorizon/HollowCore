package ru.hollowhorizon.hc.client.utils

import com.google.gson.JsonParser
import com.mojang.blaze3d.platform.NativeImage
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLPaths
import ru.hollowhorizon.hc.HollowCore.MODID
import java.net.URL
import java.util.*

object SkinDownloader {
    fun downloadSkin(skin: String): ResourceLocation {
        val hasHollowEngine = ModList.get().isLoaded("hollowengine")
        val mod = if (hasHollowEngine) "hollowengine" else MODID
        val textureLocation = ResourceLocation(mod, "skins/${skin.lowercase()}.png")
        val original =
            Minecraft.getInstance().textureManager.getTexture(textureLocation, MissingTextureAtlasSprite.getTexture())

        if (original == MissingTextureAtlasSprite.getTexture()) {
            var url = "https://api.mojang.com/users/profiles/minecraft/$skin"
            var connection = URL(url).openConnection()
            val text = connection.getInputStream().bufferedReader().readText()
            val uuid = JsonParser.parseString(text).asJsonObject.get("id").asString
            url = "https://sessionserver.mojang.com/session/minecraft/profile/$uuid"
            connection = URL(url).openConnection()
            val text2 = connection.getInputStream().bufferedReader().readText()
            val json = JsonParser.parseString(text2).asJsonObject.getAsJsonArray("properties").map { it.asJsonObject }
                .first { it.get("name").asString == "textures" }
                .get("value").asString
            val textureJson = Base64.getDecoder().decode(json)
            val textureUrl = JsonParser.parseString(String(textureJson)).asJsonObject.getAsJsonObject("textures")
                .getAsJsonObject("SKIN").get("url").asString
            val texture = URL(textureUrl).openConnection().getInputStream().readBytes()

            Minecraft.getInstance().textureManager.register(
                textureLocation, DynamicTexture(
                    NativeImage.read(texture.inputStream())
                )
            )
            if (hasHollowEngine) {
                FMLPaths.GAMEDIR.get()
                    .resolve("hollowengine/assets/hollowengine/textures/skins/${skin.lowercase()}.png").toFile()
                    .apply {
                        parentFile?.mkdirs()
                    }.writeBytes(texture)
            }
        }
        return textureLocation
    }
}

fun main() {
    SkinDownloader.downloadSkin("HollowHorizon")
}