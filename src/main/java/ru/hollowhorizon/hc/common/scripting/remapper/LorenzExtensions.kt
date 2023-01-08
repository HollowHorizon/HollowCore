package ru.hollowhorizon.hc.common.scripting.remapper

import org.cadixdev.bombe.type.signature.MethodSignature
import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.model.ClassMapping
import org.cadixdev.lorenz.model.FieldMapping
import org.cadixdev.lorenz.model.MethodMapping

fun MappingSet.findClassMapping(obfuscatedName: String): ClassMapping<*, *>? = getClassMapping(obfuscatedName).orElse(null)

fun MappingSet.findPotentialInnerClassMapping(obfuscatedName: String): ClassMapping<*, *>? {
    val idx = obfuscatedName.lastIndexOf('.')
    if (idx < 0) {
        return findClassMapping(obfuscatedName)
    }
    val outerName = obfuscatedName.substring(0, idx)
    val innerName = obfuscatedName.substring(idx + 1)
    val outer = findPotentialInnerClassMapping(outerName) ?: return findClassMapping(obfuscatedName)
    return outer.getInnerClassMapping(innerName).orElse(null)
}

fun ClassMapping<*, *>.findFieldMapping(obfuscatedName: String): FieldMapping? = getFieldMapping(obfuscatedName).orElse(null)
fun ClassMapping<*, *>.findMethodMapping(signature: MethodSignature): MethodMapping? = getMethodMapping(signature).orElse(null)