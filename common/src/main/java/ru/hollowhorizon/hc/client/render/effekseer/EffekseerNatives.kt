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

package ru.hollowhorizon.hc.client.render.effekseer

import net.minecraft.Util
import net.minecraft.client.Minecraft
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
            val root = Minecraft.getInstance().gameDirectory.resolve("hollowcore").resolve("natives")
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
