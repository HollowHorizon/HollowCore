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
            var url = "https://skins.danielraybone.com/v1/profile/$skin"
            var connection = URL(url).openConnection()
            val text = connection.getInputStream().bufferedReader().readText()
            val base64 = JsonParser.parseString(text).asJsonObject["assets"].asJsonObject["skin"].asJsonObject["base64"].asString
            val textureJson = Base64.getDecoder().decode(base64)

            Minecraft.getInstance().textureManager.register(
                textureLocation, DynamicTexture(
                    NativeImage.read(textureJson.inputStream())
                )
            )
            if (hasHollowEngine) {
                FMLPaths.GAMEDIR.get()
                    .resolve("hollowengine/assets/hollowengine/textures/skins/${skin.lowercase()}.png").toFile()
                    .apply {
                        parentFile?.mkdirs()
                    }.writeBytes(textureJson)
            }
        }
        return textureLocation
    }
}

fun main() {
    SkinDownloader.downloadSkin("HollowHorizon")
}