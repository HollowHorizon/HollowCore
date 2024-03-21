package ru.hollowhorizon.hc.particles.client.installer

import com.google.common.base.Suppliers
import net.minecraft.Util
import net.minecraftforge.fml.loading.FMLPaths
import java.io.File
import java.util.function.Supplier

enum class NativePlatform(private val libraryFormat: String, private val prefix: String = "") {
    WINDOWS(".dll"),
    LINUX(".so", "lib"),
    MACOS(".dylib", "lib");

    fun getNativeInstallPath(dllName: String) = File(INSTALL_FOLDER.get(), formatFileName(dllName))

    fun formatFileName(dllName: String) = prefix + dllName + libraryFormat

    companion object {
        @JvmStatic
        fun current() = CURRENT.get()

        private val CURRENT: Supplier<NativePlatform> = Suppliers.memoize { findCurrent() }
        private val INSTALL_FOLDER: Supplier<File> = Suppliers.memoize { findNativeFolder() }
        private fun findCurrent() = when (Util.getPlatform()) {
            Util.OS.LINUX -> LINUX
            Util.OS.SOLARIS -> throw UnsupportedOperationException("Solaris is not supported yet!")
            Util.OS.WINDOWS -> WINDOWS
            Util.OS.OSX -> MACOS
            else -> throw UnsupportedOperationException("Unknown Platform")
        }

        private fun findNativeFolder(): File {
            val root = FMLPaths.GAMEDIR.get().resolve("hollowcore").resolve("natives").toFile()
            if(!root.exists()) root.mkdirs()
            return root
        }
    }
}
