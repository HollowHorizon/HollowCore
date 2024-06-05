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

package ru.hollowhorizon.hc.client.render.effekseer.loader

import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.Resource
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.SimplePreparableReloadListener
import net.minecraft.util.profiling.ProfilerFiller
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.render.effekseer.EffekseerEffect
import ru.hollowhorizon.hc.client.render.effekseer.TextureType
import ru.hollowhorizon.hc.client.render.effekseer.EffectDefinition
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*
import java.util.function.BiConsumer
import java.util.function.IntFunction

object EffekAssets : SimplePreparableReloadListener<EffekAssets.Preparations>() {
    private const val DIRECTORY = "effeks"
    private const val FILE_TYPE = ".efkefc"

    private fun getResourceOrUseFallbackPath(
        manager: ResourceManager,
        path: ResourceLocation,
        fallback: ResourceLocation,
    ): Optional<Resource> {
        return manager.getResource(path).or { manager.getResource(fallback) }
    }

    private fun createEffekName(location: ResourceLocation): ResourceLocation {
        var filePath = location.path
        if (filePath.startsWith("$DIRECTORY/")) filePath = filePath.substring("$DIRECTORY/".length)
        if (filePath.endsWith(FILE_TYPE) || filePath.endsWith(".efkpkg")) filePath =
            filePath.substring(0, filePath.length - FILE_TYPE.length)

        return ResourceLocation(location.namespace, filePath)
    }

    private val LOGGER: Logger = LogManager.getLogger(
        EffekAssets::class.java.simpleName
    )

    private val loadedEffects: MutableMap<ResourceLocation, EffectDefinition> = LinkedHashMap()


    private fun loadEffect(
        manager: ResourceManager,
        name: ResourceLocation,
        efkefc: Resource,
    ): Optional<EffekseerEffect> {
        try {
            efkefc.open().use { input ->
                val effect = EffekseerEffect()
                val success = effect.load(input, 1f)
                if (!success) {
                    LOGGER.error("Failed to load $name")
                    return Optional.empty()
                }
                try {
                    for (texType in TextureType.values()) {
                        val count = effect.textureCount(texType)
                        load(manager,
                            name,
                            count,
                            { effect.getTexturePath(it, texType) },
                            { b: ByteArray, len: Int, i: Int ->
                                effect.loadTexture(b, len, i, texType)
                            })
                    }
                    load(manager, name, effect.modelCount(), effect::getModelPath, effect::loadModel)
                    load(manager, name, effect.curveCount(), effect::getCurvePath, effect::loadCurve)
                    load(manager, name, effect.materialCount(), effect::getMaterialPath, effect::loadMaterial)
                    return Optional.of(effect)
                } catch (ex: FileNotFoundException) {
                    LOGGER.error("Failed to load $name", ex)
                    effect.close()
                    return Optional.empty()
                }
            }
        } catch (ex: IOException) {
            HollowCore.LOGGER.error("Failed to load $name", ex)
            return Optional.empty()
        }
    }

    @Throws(IOException::class)
    private fun load(
        manager: ResourceManager,
        name: ResourceLocation, count: Int,
        pathGetter: IntFunction<String>,
        loadMethod: (ByteArray, Int, Int) -> Boolean,
    ) {
        val modid = name.namespace
        for (i in 0 until count) {
            val effekAssetPath = pathGetter.apply(i)
            val mcAssetPath = "$DIRECTORY/$effekAssetPath".replace('\\', '/').replace("//", "/")
            val fallbackMcAssetPath =
                "$DIRECTORY/${name.path.substringBeforeLast('/')}/$effekAssetPath".replace('\\', '/').replace("//", "/")

            val main = ResourceLocation(modid, mcAssetPath.lowercase().replace('-', '_'))
            val fallback = ResourceLocation(modid, fallbackMcAssetPath.lowercase().replace('-', '_'))
            val resource = getResourceOrUseFallbackPath(
                manager,
                main,
                fallback
            ).orElseThrow { FileNotFoundException("Failed to load $main or $fallback") }
            resource.open().use { input ->
                val bytes = input.readAllBytes()
                val success = loadMethod(bytes, bytes.size, i)
                if (!success) {
                    val info = String.format("Failed to load effek data %s", effekAssetPath)
                    LOGGER.debug(String.format("\n%s\nmc asset path is \"%s\"", info, mcAssetPath))
                    throw EffekLoadException(info)
                }
            }
        }
    }

    operator fun get(id: ResourceLocation) = loadedEffects[id]

    fun entries(): Set<Map.Entry<ResourceLocation, EffectDefinition>> = loadedEffects.entries

    fun forEach(action: BiConsumer<ResourceLocation, EffectDefinition>) {
        loadedEffects.forEach(action)
    }

    class Preparations {
        val loadedEffects: MutableMap<ResourceLocation, EffectDefinition> = LinkedHashMap()
    }

    private fun unloadAll() {
        loadedEffects.forEach { (_: ResourceLocation, definition: EffectDefinition) -> definition.close() }
        loadedEffects.clear()
    }

    override fun prepare(manager: ResourceManager, profilerFiller: ProfilerFiller) = Preparations()

    override fun apply(preparations: Preparations, manager: ResourceManager, profilerFiller: ProfilerFiller) {
        EffekRenderer.init()
        val prep = Preparations()
        manager.listResources(DIRECTORY) { it.path.endsWith(FILE_TYPE) }
            .forEach { (location: ResourceLocation, resource: Resource) ->
                val name = createEffekName(location)
                loadEffect(manager, name, resource).ifPresent {
                    prep.loadedEffects[name] = EffectDefinition().setEffect(it) ?: return@ifPresent
                }
            }
        unloadAll()
        loadedEffects.putAll(prep.loadedEffects)
    }
}

