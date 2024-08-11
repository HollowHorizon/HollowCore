package ru.hollowhorizon.hc.common.scripting.mappings

import net.fabricmc.mappingio.MappingReader
import net.fabricmc.mappingio.format.MappingFormat
import net.fabricmc.mappingio.tree.MemoryMappingTree
import net.fabricmc.tinyremapper.*
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.LOGGER
import java.io.*
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

object Remapper {
    private val MAPPINGS = loadMappings(
        HollowCore::class.java.getResourceAsStream("/mappings.tiny")
            ?: throw FileNotFoundException("mappings.tiny not found!")
    )
    val OBFUSCATE_REMAPPER get() = buildRemapper(createMappings(MAPPINGS, "named", "intermediary"))
    val DEOBFUSCATE_REMAPPER get() = buildRemapper(createMappings(MAPPINGS, "intermediary", "named"))

    private fun buildRemapper(mappings: IMappingProvider) = TinyRemapper.newRemapper()
        .rebuildSourceFilenames(true)
        .fixPackageAccess(false)
        .skipLocalVariableMapping(true)
        .keepInputData(false)
        .ignoreConflicts(true)
        .extension(KotlinMetadataTinyRemapperExtension)
        .withMappings(mappings)
        .build()


    private fun createMappings(tree: MemoryMappingTree, from: String, to: String): IMappingProvider {
        return TinyUtils.createMappingProvider(tree, from, to)
    }

    private fun loadMappings(stream: InputStream) = MemoryMappingTree().apply {
        MappingReader.read(InputStreamReader(stream), MappingFormat.TINY_2_FILE, this)
    }


    fun remap(remapper: TinyRemapper, filesArray: Array<File>, newDir: Path, vararg classPath: Path) {
        val files: MutableMap<Path, InputTag> = mutableMapOf()
        filesArray.forEach { file ->
            try {
                if (!newDir.resolve(file.name).exists()) {
                    files[file.toPath()] = remapper.createInputTag()
                }
            } catch (e: IOException) {
                LOGGER.error("Error while copying $file", e)
            }
        }

        remapper.readInputsAsync(remapper.createInputTag(), *classPath)

        val consumers: MutableMap<OutputConsumerPath, InputTag> = mutableMapOf()

        for ((file, tag) in files) {
            try {
                val consumer = OutputConsumerPath.Builder(newDir.resolve(file.name))
                    .assumeArchive(true)
                    .build().apply { consumers[this] = tag }

                consumer.addNonClassFiles(file, NonClassCopyMode.FIX_META_INF, remapper)
                remapper.readInputsAsync(tag, file)
            } catch (e: Exception) {
                LOGGER.error("Error while reading $file", e)
            }
        }
        consumers.forEach { (consumer, tag) ->
            try {
                remapper.apply(consumer, tag)
            } catch (e: Exception) {
                LOGGER.error("Error while applying remapper", e)
            }
        }

        if (files.isNotEmpty()) remapper.finish()
        for (consumer in consumers.keys) consumer.close()
    }
}