package ru.hollowhorizon.hc.common.scripting.remapper.legacy

import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.io.MappingsReader

class LegacyMappingsReader(private val map: Map<String, LegacyMapping>) : MappingsReader() {

    override fun read(): MappingSet {
        return read(MappingSet.create(LegacyMappingSetModelFactory()))
    }

    override fun read(mappings: MappingSet): MappingSet {
        require(mappings.modelFactory is LegacyMappingSetModelFactory) { "legacy mappings must use legacy model factory, use read() instead" }
        for (legacyMapping in map.values) {
            val classMapping = mappings.getOrCreateClassMapping(legacyMapping.oldName)
                .setDeobfuscatedName(legacyMapping.newName)
            for ((key, value) in legacyMapping.fields) {
                classMapping.getOrCreateFieldMapping(key).deobfuscatedName = value
            }
            for ((key, value) in legacyMapping.methods) {
                classMapping.getOrCreateMethodMapping(key, "()V").deobfuscatedName = value
            }
        }
        return mappings
    }

    override fun close() {}
}