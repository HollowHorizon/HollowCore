package ru.hollowhorizon.hc.common.scripting.mappings


import kotlinx.serialization.Serializable
import org.jetbrains.kotlin.load.java.structure.JavaClass
import org.jetbrains.kotlin.load.java.structure.JavaClassifier
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.loadAsNBT

object HollowMappings {
    val MAPPINGS = NBTFormat.deserialize<Mappings>(
        (HollowMappings.javaClass.getResourceAsStream("/mappings.nbt")
            ?: throw IllegalStateException("Mappings file not found!"))
            .loadAsNBT()
    )
}

@Serializable
data class Mappings(val mappings: HashSet<ClassMapping>) {
    operator fun get(callerClass: JavaClassifier?): ClassMapping? {
        if (callerClass !is JavaClass) return null

        val mapping = mappings.find { it.mcpName == callerClass.fqName!!.asString() }

        if (mapping == null) {
            callerClass.supertypes.filter { it.classifier != null }.forEach {
                val result = get(it.classifier!!)
                if (result != null) return result
            }
            return null
        }

        return mapping
    }
}

@Serializable
data class ClassMapping(
    val mcpName: String,
    val srgName: String
) {
    fun fieldObf(text: String): String {
        return fields.find { it.mcpName == text }?.srgName ?: text
    }

    fun fieldDeobf(text: String): String {
        return fields.find { it.srgName == text }?.mcpName ?: text
    }

    fun method(text: String): String {
        return methods.find { it.mcpName == text }?.srgName ?: text
    }

    fun methodsObf(deobfName: String): List<String> {
        return methods.filter { it.mcpName == deobfName }.map { it.srgName }
    }

    val fields = HashSet<FieldMapping>()
    val methods = HashSet<MethodMapping>()
}

@Serializable
data class FieldMapping(
    val mcpName: String,
    val srgName: String
)

@Serializable
data class MethodMapping(
    val mcpName: String,
    val srgName: String,
    val params: String
)
