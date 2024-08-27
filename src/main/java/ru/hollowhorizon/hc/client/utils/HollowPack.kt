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

//? if >=1.21 {
/*import net.minecraft.server.packs.PackLocationInfo
import net.minecraft.server.packs.PackSelectionConfig
*///?}
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.server.packs.PackType
import net.minecraft.server.packs.metadata.MetadataSectionSerializer
import net.minecraft.server.packs.repository.Pack
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier
import net.minecraft.server.packs.repository.PackSource
import net.minecraft.server.packs.resources.IoSupplier
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*

object HollowPack : PackResources {
    private val genSounds = HashMap<String, JsonObject>()
    val genItemModels = ArrayList<ResourceLocation>()
    val genBlockData = ArrayList<ResourceLocation>()
    private val genParticles = ArrayList<ResourceLocation>()
    private val resourceMap = HashMap<ResourceLocation, IoSupplier<InputStream>?>()

    init {
        close() // Uses as reload
    }

    private fun ofText(text: String) = IoSupplier<InputStream> { ByteArrayInputStream(text.toByteArray()) }
    fun generatePostShader(location: ResourceLocation) {
        resourceMap["${location.namespace}:shaders/post/${location.path}.json".rl] =
            ofText("{\"targets\": [\"swap\"],\"passes\": [{\"name\": \"$location\",\"intarget\": \"minecraft:main\",\"outtarget\": \"swap\",\"uniforms\": []},{\"name\": \"$location\",\"intarget\": \"swap\",\"outtarget\": \"minecraft:main\",\"uniforms\": []}]}")
        resourceMap["${location.namespace}:shaders/program/${location.path}.json".rl] =
            ofText("{\"blend\":{\"func\":\"add\",\"srcrgb\":\"one\",\"dstrgb\":\"zero\"},\"vertex\":\"sobel\",\"fragment\":\"$location\",\"attributes\":[\"Position\"],\"samplers\":[{\"name\":\"DiffuseSampler\"}],\"uniforms\":[{\"name\":\"ProjMat\",\"type\":\"matrix4x4\",\"count\":16,\"values\":[1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0]},{\"name\":\"InSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"OutSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"Time\",\"type\":\"float\",\"count\":1,\"values\":[0.0]}]}")
    }

    private fun addItemModel(location: ResourceLocation) {
        val modelLocation = "${location.namespace}:models/item/${location.path}.json".rl
        resourceMap[modelLocation] =
            ofText("{\"parent\":\"item/handheld\",\"textures\":{\"layer0\":\"" + location.namespace + ":item/" + location.path + "\"}}")
    }

    private fun addParticleModel(location: ResourceLocation) {
        val particle = "${location.namespace}:particles/${location.path}.json".rl
        resourceMap[particle] = ofText("{\"textures\":[\"$location\"]}")
    }

    private fun addBlockModel(location: ResourceLocation) {
        val blockstate = "${location.namespace}:blockstates/${location.path}.json".rl
        val model = "${location.namespace}:models/item/${location.path}.json".rl
        resourceMap[blockstate] =
            ofText("{\"variants\":{\"\":{\"model\":\"" + location.namespace + ":item/" + location.path + "\"}}}")
        resourceMap[model] =
            ofText("{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\"" + location.namespace + ":blocks/" + location.path + "\"}}")
    }

    private fun addSoundJson(modid: String, sound: JsonObject) {
        resourceMap["${modid}:sounds.json".rl] = ofText(sound.toString())
    }

    override fun getRootResource(vararg fileName: String): IoSupplier<InputStream> {
        throw FileNotFoundException(fileName.joinToString())
    }

    @Throws(IOException::class)
    override fun getResource(type: PackType, pLocation: ResourceLocation): IoSupplier<InputStream>? {
        return resourceMap[pLocation]
    }

    override fun listResources(
        packType: PackType,
        namespace: String,
        prefix: String,
        output: PackResources.ResourceOutput,
    ) {
        resourceMap.filter { it.key.namespace == namespace && it.key.path.startsWith(prefix) }.forEach(output::accept)
    }

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

    //? if <1.21 {
    override fun packId(): String {
        return "HollowCore Resources"
    }
    //?} else {
    
    /*override fun location(): PackLocationInfo {
        val name = "HollowCore Resources"
        return PackLocationInfo(name, name.mcText, PackSource.BUILT_IN, Optional.empty())
    }

    *///?}


    override fun close() {
        resourceMap.clear()

        genItemModels.forEach(::addItemModel)
        genParticles.forEach(::addParticleModel)
        genBlockData.forEach(::addBlockModel)
        genSounds.forEach(::addSoundJson)
    }

    val resources = asPack()
}

fun PackResources.asPack(): Pack {
    //? if <1.21 {
    val resources = ResourcesSupplier { this }
    return Pack.readMetaAndCreate(
        packId(), packId().literal, true, resources, PackType.CLIENT_RESOURCES,
        Pack.Position.TOP, PackSource.BUILT_IN
    ) ?: throw FileNotFoundException("Could not find the pack resource $this")
    //?} else {
    
    /*val resources = object : ResourcesSupplier {
        override fun openPrimary(p0: PackLocationInfo): PackResources = this@asPack
        override fun openFull(p0: PackLocationInfo, p1: Pack.Metadata) = this@asPack
    }
    return Pack.readMetaAndCreate(
        this.location(),
        resources,
        PackType.CLIENT_RESOURCES,
        PackSelectionConfig(true, Pack.Position.TOP, true)
    ) ?: throw FileNotFoundException("Could not find the pack resource $this")
    *///?}
}