package ru.hollowhorizon.hc.common.scripting.mappings


import kotlinx.serialization.Serializable
import org.objectweb.asm.tree.ClassNode
import ru.hollowhorizon.hc.client.utils.nbt.MAPPINGS_SERIALIZER
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.loadAsNBT
import java.io.DataInputStream
import java.io.InputStream

object HollowMappings {
    @JvmField
    val MAPPINGS = MAPPINGS_SERIALIZER.deserialize<Mappings>(run {
        val stream = (HollowMappings.javaClass.getResourceAsStream("/mappings.nbt")
            ?: throw IllegalStateException("Mappings file not found!"))

        return@run DataInputStream(stream).loadAsNBT()
    }
    )
}

fun main() {
    val mapping = Mappings.loadFromTSRG(Mappings.Companion::class.java.getResourceAsStream("/output.tsrg")!!)

    println(mapping)
}

@Serializable
data class Mappings(val mappings: HashSet<ClassMapping>) {
    companion object {
        fun loadFromTSRG(stream: InputStream): Mappings {
            val mappings = HashSet<ClassMapping>()
            var lastClass: ClassMapping? = null
            stream.bufferedReader().forEachLine { line ->
                if (!line.startsWith("\t")) { //Если идёт класс
                    val data = line.replace("/", ".").replace("$", ".").split(" ")
                    mappings.add(ClassMapping(data[0], data[1]).apply { lastClass = this })
                } else if (!line.startsWith("\t\t")) { //Если идёт метод или параметр
                    val data = line.substringAfter("\t").replace("/", ".").replace("$", ".").split(" ")
                    if(data.size == 3) lastClass?.methods?.add(MethodMapping(data[0], data[2], data[1]))
                    else lastClass?.fields?.add(FieldMapping(data[0], data[1]))
                }
            }
            stream.close()
            return Mappings(mappings)
        }
    }

    val fields = mappings.flatMap { it.fields.map { f -> f.srgName to f.mcpName } }.toMap()
    val methods = mappings.flatMap { it.methods.map { m -> m.srgName to m.mcpName } }.toMap()

    operator fun get(node: ClassNode) = mappings.find { it.mcpName == node.name.replace("/", ".").replace("$", ".") }

    fun fieldObf(node: ClassNode, name: String): String {
        val newName = this[node]?.fieldObf(name) ?: name

        if (newName == name) {
            (node.interfaces + node.superName).filterNotNull().mapNotNull { it.node }.forEach { supNode ->
                val fName = fieldObf(supNode, name)
                if (fName != name) return fName
            }
        }
        return newName
    }

    fun methodObf(node: ClassNode, name: String, signature: String): String {
        val newName = this[node]?.methodObf(name, signature) ?: name

        if (newName == name) {
            (node.interfaces + node.superName).filterNotNull().mapNotNull { it.node }.forEach { supNode ->
                val fName = methodObf(supNode, name, signature)
                if (fName != name) return fName
            }
        }
        return newName
    }

    fun fieldDeobf(name: String) = this.fields.getOrDefault(name, name)
    fun methodDeobf(name: String) = this.methods.getOrDefault(name, name)
}

@Serializable
data class ClassMapping(
    val mcpName: String,
    val srgName: String
) {
    val fields = HashSet<FieldMapping>()
    val methods = HashSet<MethodMapping>()

    fun fieldObf(text: String): String {
        return fields.find { it.mcpName == text }?.srgName ?: text
    }

    fun methodObf(deobfName: String, signature: String): String {
        return methods.firstOrNull {
            it.mcpName == deobfName && it.params == signature.replace("/", ".").replace("$", ".")
        }?.srgName ?: deobfName
    }
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