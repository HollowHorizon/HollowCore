/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.hollowhorizon.hc.client.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.SharedConstants
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraft.server.packs.metadata.pack.PackMetadataSection
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.function.Predicate

object HollowPack : PackResources {
    private val genSounds = HashMap<String, JsonObject>()
    val genItemModels = ArrayList<ResourceLocation>()
    val genBlockData = ArrayList<ResourceLocation>()
    private val genParticles = ArrayList<ResourceLocation>()
    private val resourceMap = HashMap<ResourceLocation, () -> InputStream?>()

    init {
        close() // Uses as reload
    }

    private fun ofText(text: String): () -> InputStream = { ByteArrayInputStream(text.toByteArray()) }
    fun generatePostShader(location: ResourceLocation) {
        resourceMap[ResourceLocation(location.namespace, "shaders/post/" + location.path + ".json")] =
            ofText("{\"targets\": [\"swap\"],\"passes\": [{\"name\": \"$location\",\"intarget\": \"minecraft:main\",\"outtarget\": \"swap\",\"uniforms\": []},{\"name\": \"$location\",\"intarget\": \"swap\",\"outtarget\": \"minecraft:main\",\"uniforms\": []}]}")
        resourceMap[ResourceLocation(
            location.namespace,
            "shaders/program/" + location.path + ".json"
        )] =
            ofText("{\"blend\":{\"func\":\"add\",\"srcrgb\":\"one\",\"dstrgb\":\"zero\"},\"vertex\":\"sobel\",\"fragment\":\"$location\",\"attributes\":[\"Position\"],\"samplers\":[{\"name\":\"DiffuseSampler\"}],\"uniforms\":[{\"name\":\"ProjMat\",\"type\":\"matrix4x4\",\"count\":16,\"values\":[1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0]},{\"name\":\"InSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"OutSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"Time\",\"type\":\"float\",\"count\":1,\"values\":[0.0]}]}")
    }

    private fun addItemModel(location: ResourceLocation) {
        val modelLocation = ResourceLocation(location.namespace, "models/item/" + location.path + ".json")
        resourceMap[modelLocation] =
            ofText("{\"parent\":\"item/handheld\",\"textures\":{\"layer0\":\"" + location.namespace + ":items/" + location.path + "\"}}")
    }

    private fun addParticleModel(location: ResourceLocation) {
        val particle = ResourceLocation(location.namespace, "particles/" + location.path + ".json")
        resourceMap[particle] = ofText("{\"textures\":[\"$location\"]}")
    }

    private fun addBlockModel(location: ResourceLocation) {
        val blockstate = ResourceLocation(location.namespace, "blockstates/" + location.path + ".json")
        val model = ResourceLocation(location.namespace, "models/item/" + location.path + ".json")
        resourceMap[blockstate] =
            ofText("{\"variants\":{\"\":{\"model\":\"" + location.namespace + ":item/" + location.path + "\"}}}")
        resourceMap[model] =
            ofText("{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\"" + location.namespace + ":blocks/" + location.path + "\"}}")
    }

    private fun addSoundJson(modid: String, sound: JsonObject) {
        resourceMap[ResourceLocation(modid, "sounds.json")] =
            ofText(sound.toString())
    }

    @Throws(IOException::class)
    override fun getRootResource(fileName: String): InputStream? {
        throw FileNotFoundException(fileName)
    }

    @Throws(IOException::class)
    override fun getResource(type: PackType, pLocation: ResourceLocation): InputStream {
        return resourceMap[pLocation]?.invoke() ?: throw FileNotFoundException("Resource $pLocation not found!")
    }

    override fun getResources(
        pType: PackType,
        pNamespace: String,
        pPath: String,
        pFilter: Predicate<ResourceLocation>,
    ): Collection<ResourceLocation> = emptyList()

    override fun hasResource(pType: PackType, pLocation: ResourceLocation) = resourceMap[pLocation] != null
    override fun getNamespaces(pType: PackType) = resourceMap.keys.map { it.namespace }.toSet()

    override fun <T> getMetadataSection(pDeserializer: MetadataSectionSerializer<T>): T? {
        if (pDeserializer.metadataSectionName == "pack") {
            //var - java 16 feature
            val obj = JsonObject()
            val supportedFormats = JsonArray()
            (6..9).forEach(supportedFormats::add) // From 1.16.2-rc1 to 1.19.3
            obj.addProperty("pack_format", 9)
            obj.add("supported_formats", supportedFormats)
            obj.addProperty("description", "Generated resources for HollowCore")
            return pDeserializer.fromJson(obj)
        }
        return null
    }

    override fun getName() = "Hollow Core Generated Resources"

    val section = PackMetadataSection(
        Component.translatable("fml.resources.modresources", 1),
        PackType.CLIENT_RESOURCES.getVersion(SharedConstants.getCurrentVersion())
    )

    override fun close() {
        resourceMap.clear()

        genItemModels.forEach(::addItemModel)
        genParticles.forEach(::addParticleModel)
        genBlockData.forEach(::addBlockModel)
        genSounds.forEach(::addSoundJson)
    }
}
