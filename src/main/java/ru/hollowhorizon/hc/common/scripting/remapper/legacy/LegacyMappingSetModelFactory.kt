package ru.hollowhorizon.hc.common.scripting.remapper.legacy

import org.cadixdev.bombe.type.signature.MethodSignature
import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.impl.MappingSetModelFactoryImpl
import org.cadixdev.lorenz.impl.model.TopLevelClassMappingImpl
import org.cadixdev.lorenz.model.MethodMapping
import org.cadixdev.lorenz.model.TopLevelClassMapping
import java.util.*

class LegacyMappingSetModelFactory : MappingSetModelFactoryImpl() {
    override fun createTopLevelClassMapping(parent: MappingSet, obfuscatedName: String, deobfuscatedName: String): TopLevelClassMapping {
        return object : TopLevelClassMappingImpl(parent, obfuscatedName, deobfuscatedName) {
            private fun stripDesc(signature: MethodSignature): MethodSignature {
                // actual descriptor isn't included in legacy format
                return MethodSignature.of(signature.name, "()V")
            }

            override fun hasMethodMapping(signature: MethodSignature): Boolean {
                return super.hasMethodMapping(signature) || super.hasMethodMapping(stripDesc(signature))
            }

            override fun getMethodMapping(signature: MethodSignature): Optional<MethodMapping> {
                var maybeMapping = super.getMethodMapping(signature)
                if (!maybeMapping.isPresent || maybeMapping.get().let { it.signature == it.deobfuscatedSignature }) {
                    maybeMapping = super.getMethodMapping(stripDesc(signature))
                }
                return maybeMapping
            }
        }
    }
}