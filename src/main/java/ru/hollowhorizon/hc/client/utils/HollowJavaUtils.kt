package ru.hollowhorizon.hc.client.utils

import net.minecraft.client.Minecraft
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

object HollowJavaUtils {
    @JvmStatic
    @OnlyIn(Dist.CLIENT)
    fun getResource(location: ResourceLocation): InputStream {
        return try {
            Minecraft.getInstance().resourceManager.getResource(location).inputStream
        } catch (e: Exception) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("assets/" + location.namespace + "/" + location.path) ?: throw FileNotFoundException("Resource $location not found!")
        }
    }

    fun hasResource(location: ResourceLocation): Boolean {
        return try {
            Minecraft.getInstance().resourceManager.hasResource(location)
        } catch (ex: Exception) {
            Thread.currentThread().contextClassLoader.getResourceAsStream("assets/" + location.namespace + "/" + location.path) != null
        }
    }



    @JvmStatic
    fun initPath(file: File) {
        file.parentFile.mkdirs()
        file.createNewFile()
    }
}
