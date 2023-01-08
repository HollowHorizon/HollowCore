package ru.hollowhorizon.hc.common.scripting.remapper.legacy

import org.cadixdev.lorenz.MappingSet
import java.io.*

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.HashMap

class LegacyMapping(var oldName: String, var newName: String) {
    var fields: MutableMap<String, String> = mutableMapOf()
    var methods: MutableMap<String, String> = mutableMapOf()

    companion object {
        @Throws(IOException::class)
        fun readMappingSet(mappingFile: Path, invert: Boolean): MappingSet {
            return LegacyMappingsReader(readMappings(mappingFile, invert)).read()
        }

        @Throws(IOException::class)
        fun readMappingSet(fileName: String, reader: Reader, invert: Boolean): MappingSet {
            return LegacyMappingsReader(readMappings(fileName, reader, invert)).read()
        }

        @Throws(IOException::class)
        fun readMappings(mappingFile: Path, invert: Boolean): Map<String, LegacyMapping> {
            Files.newBufferedReader(mappingFile, StandardCharsets.UTF_8).use {
                return readMappings(mappingFile.toString(), it, invert)
            }
        }

        @Throws(IOException::class)
        fun readMappings(fileName: String, reader: Reader, invert: Boolean): Map<String, LegacyMapping> {
            val mappings = HashMap<String, LegacyMapping>()
            val revMappings = HashMap<String, LegacyMapping>()
            var lineNumber = 0
            for (line in BufferedReader(reader).lineSequence()) {
                lineNumber++
                if (line.trim { it <= ' ' }.startsWith("#") || line.trim { it <= ' ' }.isEmpty()) continue

                val parts = line.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                require(!(parts.size < 2 || line.contains(";"))) { "Failed to parse line $lineNumber in $fileName." }

                var mapping: LegacyMapping? = mappings[parts[0]]
                if (mapping == null) {
                    mapping = LegacyMapping(parts[0], parts[0])
                    mappings[mapping.oldName] = mapping
                }

                if (parts.size == 2) {
                    // Class mapping
                    mapping.newName = parts[1]
                    // Possibly merge with reverse mapping
                    val revMapping = revMappings.remove(mapping.newName)
                    if (revMapping != null) {
                        mapping.fields.putAll(revMapping.fields)
                        mapping.methods.putAll(revMapping.methods)
                    }
                    revMappings[mapping.newName] = mapping
                } else if (parts.size == 3 || parts.size == 4) {
                    var fromName = parts[1]
                    var toName: String
                    var revMapping: LegacyMapping?
                    if (parts.size == 4) {
                        toName = parts[3]
                        revMapping = revMappings[parts[2]]
                        if (revMapping == null) {
                            revMapping = LegacyMapping(parts[2], parts[2])
                            revMappings[revMapping.newName] = revMapping
                        }
                    } else {
                        toName = parts[2]
                        revMapping = mapping
                    }
                    if (fromName.endsWith("()")) {
                        // Method mapping
                        fromName = fromName.substring(0, fromName.length - 2)
                        toName = toName.substring(0, toName.length - 2)
                        mapping.methods[fromName] = toName
                        revMapping.methods[fromName] = toName
                    } else {
                        // Field mapping
                        mapping.fields[fromName] = toName
                        revMapping.fields[fromName] = toName
                    }
                } else {
                    throw IllegalArgumentException("Failed to parse line $lineNumber in $fileName.")
                }
            }
            if (invert) {

                (mappings.values + revMappings.values).distinct().forEach { mapping ->
                    mapping.oldName = mapping.newName.also { mapping.newName = mapping.oldName }
                    mapping.fields = mapping.fields.map { it.value to it.key }.toMap(mutableMapOf())
                    mapping.methods = mapping.methods.map { it.value to it.key }.toMap(mutableMapOf())
                }
            }
            val result = mutableMapOf<String, LegacyMapping>()
            for (mapping in (mappings.values + revMappings.values)) {
                val key = mapping.oldName
                val other = result[key]
                result[key] = if (other != null) {
                    if (other.oldName != other.newName) {
                        require(mapping.oldName == mapping.newName || other.oldName == mapping.oldName || other.newName == mapping.newName) {
                            "Conflicting mappings: ${mapping.oldName} -> ${mapping.newName} and ${other.oldName} -> ${other.newName}"
                        }
                        mapping.oldName = other.oldName
                        mapping.newName = other.newName
                    }
                    mapping.fields.putAll(other.fields)
                    mapping.methods.putAll(other.methods)
                    mapping
                } else {
                    mapping
                }
            }
            return result
        }
    }
}