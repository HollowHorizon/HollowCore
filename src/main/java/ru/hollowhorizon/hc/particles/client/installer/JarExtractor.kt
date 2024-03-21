package ru.hollowhorizon.hc.particles.client.installer

import java.io.File
import java.io.IOException
import java.util.*

object JarExtractor {
    @JvmStatic
    @Throws(IOException::class)
    fun extract(from: String, targetFile: File) {
        val out = targetFile.outputStream()
        JarExtractor::class.java.classLoader.getResource(from)?.openStream()?.transferTo(out)
            ?: throw IOException("Failed to extract $from to $targetFile")
        out.close()

    }
}
