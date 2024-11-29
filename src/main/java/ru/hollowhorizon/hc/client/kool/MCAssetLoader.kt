package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.*
import de.fabmax.kool.modules.audio.AudioClipImpl
import de.fabmax.kool.pipeline.TextureData2d
import de.fabmax.kool.pipeline.TextureProps
import de.fabmax.kool.platform.HttpCache
import de.fabmax.kool.platform.imageAtlasTextureData
import de.fabmax.kool.util.Uint8Buffer
import de.fabmax.kool.util.Uint8BufferImpl
import de.fabmax.kool.util.logE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.common.coroutines.clientDispatcher
import java.io.FileInputStream
import kotlin.jvm.optionals.getOrNull

object MCAssetLoader : AssetLoader() {
    override suspend fun loadAudioClip(audioRef: AudioClipRef): LoadedAudioClipAsset {
        val blob = loadBlob(BlobAssetRef(audioRef.path))
        val clip = blob.data?.let { buf ->
            AudioClipImpl(buf.toArray(), audioRef.path.substringAfterLast('.').lowercase())
        }
        return LoadedAudioClipAsset(audioRef, clip)
    }

    override suspend fun loadBlob(blobRef: BlobAssetRef): LoadedBlobAsset {
        return if (blobRef.isHttp) {
            loadHttpBlob(blobRef)
        } else {
            loadLocalBlob(blobRef)
        }
    }

    private suspend fun loadLocalBlob(localRawRef: BlobAssetRef): LoadedBlobAsset {
        var data: Uint8Buffer? = null
        withContext(clientDispatcher) {
            try {
                val resource = Minecraft.getInstance().resourceManager.getResource(ResourceLocation(localRawRef.path))
                    .orElseThrow()
                data = Uint8BufferImpl(resource.open().use { it.readBytes() })
            } catch (e: Exception) {
                logE { "Failed loading asset ${localRawRef.path}: $e" }
            }
        }
        return LoadedBlobAsset(localRawRef, data)
    }

    suspend fun loadLocalTexture(path: String, props: TextureProps?): TextureData2d? = withContext(clientDispatcher) {
        Minecraft.getInstance().resourceManager.getResource(ResourceLocation(path)).getOrNull()?.open()?.use {
                try {
                    PlatformAssetsImpl.readImageData(it, MimeType.forFileName(path), props)
                } catch (e: Exception) {
                    logE { "Failed reading image at $path: $e" }
                    null
                }
            }
    }

    override suspend fun loadTexture(textureRef: TextureAssetRef): LoadedTextureAsset {
        val refCopy = TextureData2dRef(textureRef.path, textureRef.props)
        val texData = loadTextureData2d(refCopy).data as TextureData2d?
        return LoadedTextureAsset(textureRef, texData)
    }

    override suspend fun loadTextureAtlas(textureRef: TextureAtlasAssetRef): LoadedTextureAsset {
        val refCopy = TextureData2dRef(textureRef.path, textureRef.props)
        val texData = loadTextureData2d(refCopy).data as TextureData2d?
        val atlasData = texData?.let {
            imageAtlasTextureData(it, textureRef.tilesX, textureRef.tilesY)
        }
        return LoadedTextureAsset(textureRef, atlasData)    }

    override suspend fun loadTextureData2d(textureData2dRef: TextureData2dRef): LoadedTextureAsset {
        val data: TextureData2d? = withContext(Dispatchers.IO) {
            if (textureData2dRef.isHttp) {
                loadHttpTexture(textureData2dRef.path, textureData2dRef.props)
            } else {
                loadLocalTexture(textureData2dRef.path, textureData2dRef.props)
            }
        }
        return LoadedTextureAsset(textureData2dRef, data)
    }

    private suspend fun loadHttpBlob(httpRawRef: BlobAssetRef): LoadedBlobAsset {
        var data: Uint8Buffer? = null
        if (httpRawRef.path.startsWith("data:", true)) {
            data = decodeDataUri(httpRawRef.path)
        } else {
            withContext(Dispatchers.IO) {
                try {
                    HttpCache.loadHttpResource(httpRawRef.path)?.let { f ->
                        FileInputStream(f).use { data = Uint8BufferImpl(it.readBytes()) }
                    }
                } catch (e: Exception) {
                    logE { "Failed loading asset ${httpRawRef.path}: $e" }
                }
            }
        }
        return LoadedBlobAsset(httpRawRef, data)
    }

    private fun loadHttpTexture(path: String, props: TextureProps?): TextureData2d? {
        return HttpCache.loadHttpResource(path)?.let { f ->
            try {
                FileInputStream(f).use { data ->
                    PlatformAssetsImpl.readImageData(data, MimeType.forFileName(path), props)
                }
            } catch (e: Exception) {
                logE { "Failed reading image at $path: $e" }
                null
            }
        }
    }
}