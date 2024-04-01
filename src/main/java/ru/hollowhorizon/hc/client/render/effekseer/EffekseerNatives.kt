package ru.hollowhorizon.hc.client.render.effekseer

import net.minecraft.Util
import net.minecraftforge.fml.loading.FMLPaths
import ru.hollowhorizon.hc.HollowCore
import java.io.File
import java.io.IOException

enum class EffekseerNatives(private val libraryFormat: String) {
    WINDOWS(".dll"),
    LINUX(".so"),
    MACOS(".dylib");

    fun getNativeInstallPath(dllName: String) = File(installFolder, formatFileName(dllName))

    fun formatFileName(dllName: String) = dllName+libraryFormat

    companion object {
        private const val DLL_NAME = "EffekseerNativeForJava"
        private val installFolder by lazy { findNativeFolder() }
        val current by lazy { findCurrent() }
        private fun findCurrent() = when (Util.getPlatform()) {
            Util.OS.LINUX -> LINUX
            Util.OS.SOLARIS -> throw UnsupportedOperationException("Solaris is not supported yet!")
            Util.OS.WINDOWS -> WINDOWS
            Util.OS.OSX -> MACOS
            else -> throw UnsupportedOperationException("Unknown Platform")
        }

        private fun findNativeFolder(): File {
            val root = FMLPaths.GAMEDIR.get().resolve("hollowcore").resolve("natives").toFile()
            if (!root.exists()) root.mkdirs()
            return root
        }

        @JvmStatic
        @Throws(IOException::class)
        fun install(
            installPath: File = current.getNativeInstallPath(DLL_NAME),
        ) {
            if (!installPath.isFile) {
                HollowCore.LOGGER.info("Installing Effekseer native library at " + installPath.canonicalPath)

                val from = current.formatFileName(DLL_NAME)
                val out = installPath.outputStream()
                EffekseerNatives::class.java.classLoader.getResource("natives/$from")?.openStream()?.transferTo(out)
                    ?: throw IOException("Failed to extract $from to $installPath")
                out.close()
            } else {
                HollowCore.LOGGER.debug("Loading Effekseer native library at " + installPath.canonicalPath)
            }


            System.load(installPath.canonicalPath)
        }
    }
}
