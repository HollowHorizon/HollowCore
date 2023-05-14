package ru.hollowhorizon.hc.client.ultralight

import net.minecraft.util.Util
import net.minecraft.util.Util.OS
import net.minecraftforge.fml.loading.FMLPaths
import ru.hollowhorizon.hc.HollowCore
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess

class UltralightResources {

    companion object {
        private const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
        private const val LIBRARY_VERSION = "b8daecd_0.4.12"
    }

    val ultralightRoot = File(FMLPaths.GAMEDIR.get().toFile(), "ultralight")
    val binRoot = File(ultralightRoot, "bin")
    val cacheRoot = File(ultralightRoot, "cache")
    val resourcesRoot = File(ultralightRoot, "resources")

    fun downloadResources() {
        runCatching {
            val versionsFile = File(ultralightRoot, "VERSION")

            // Check if library version is matching the resources version
            if (versionsFile.exists() && versionsFile.readText() == LIBRARY_VERSION) {
                return
            }

            // Make sure the old natives are being deleted
            if (binRoot.exists()) {
                binRoot.deleteRecursively()
            }

            if (resourcesRoot.exists()) {
                resourcesRoot.deleteRecursively()
            }

            // Translate os to path
            val os = when (Util.getPlatform()) {
                OS.WINDOWS -> "win"
                OS.OSX -> "mac"
                OS.LINUX -> "linux"
                else -> error("unsupported operating system")
            }

            HollowCore.LOGGER.info("Downloading v$LIBRARY_VERSION resources... (os: $os)")
            val nativeUrl = "$CLIENT_CLOUD/ultralight_resources/$LIBRARY_VERSION/$os-x64.zip"

            // Download resources
            ultralightRoot.mkdir()
            val pkgNatives = File(ultralightRoot, "resources.zip").apply {
                createNewFile()
                HttpClient.download(nativeUrl, this)
            }

            // Extract resources from zip archive
            HollowCore.LOGGER.info("Extracting resources...")
            extractZip(pkgNatives, ultralightRoot)
            versionsFile.createNewFile()
            versionsFile.writeText(LIBRARY_VERSION.toString())

            // Make sure to delete zip archive to save space
            HollowCore.LOGGER.debug("Deleting resources bundle...")
            pkgNatives.delete()

            HollowCore.LOGGER.info("Successfully loaded resources.")
        }.onFailure {
            HollowCore.LOGGER.error("Unable to download resources", it)

            exitProcess(-1)
        }
    }

}

fun extractZip(zipStream: InputStream, folder: File) {
    if (!folder.exists()) {
        folder.mkdir()
    }

    ZipInputStream(zipStream).use { zipInputStream ->
        var zipEntry = zipInputStream.nextEntry

        while (zipEntry != null) {
            if (zipEntry.isDirectory) {
                zipEntry = zipInputStream.nextEntry
                continue
            }

            val newFile = File(folder, zipEntry.name)
            File(newFile.parent).mkdirs()

            FileOutputStream(newFile).use {
                zipInputStream.copyTo(it)
            }
            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.closeEntry()
    }
}

fun extractZip(zipFile: File, folder: File) = extractZip(FileInputStream(zipFile), folder)

object HttpClient {

    private const val DEFAULT_AGENT =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.60"

    init {
        HttpURLConnection.setFollowRedirects(true)
    }

    private fun make(url: String, method: String, agent: String = DEFAULT_AGENT): HttpURLConnection {
        val httpConnection = URL(url).openConnection() as HttpURLConnection

        httpConnection.requestMethod = method
        httpConnection.connectTimeout = 2000 // 2 seconds until connect timeouts
        httpConnection.readTimeout = 10000 // 10 seconds until read timeouts

        httpConnection.setRequestProperty("User-Agent", agent)

        httpConnection.instanceFollowRedirects = true
        httpConnection.doOutput = true

        return httpConnection
    }

    fun request(url: String, method: String, agent: String = DEFAULT_AGENT): String {
        val connection = make(url, method, agent)

        return connection.inputStream.reader().readText()
    }

    fun requestStream(url: String, method: String, agent: String = DEFAULT_AGENT): InputStream {
        val connection = make(url, method, agent)

        return connection.inputStream
    }

    fun get(url: String) = request(url, "GET")

    fun download(url: String, file: File) = FileOutputStream(file).use { make(url, "GET").inputStream.copyTo(it) }

}

