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

package ru.hollowhorizon.hc.common.scripting.mappings


import kotlinx.serialization.Serializable
import org.objectweb.asm.tree.ClassNode
import ru.hollowhorizon.hc.client.utils.nbt.*
import java.io.DataInputStream
import java.io.File
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

    NBTFormat.serialize(mapping).save(File("mappings.nbt").outputStream())

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
                    if (data.size == 3) lastClass?.methods?.add(MethodMapping(data[0], data[2], data[1]))
                    else lastClass?.fields?.add(FieldMapping(data[0], data[1]))
                }
            }
            stream.close()
            return Mappings(mappings)
        }
    }

    @kotlinx.serialization.Transient
    val fields = mappings.flatMap { it.fields.map { f -> f.srgName to f.mcpName } }.toMap()

    @kotlinx.serialization.Transient
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
    val srgName: String,
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
    val srgName: String,
)

@Serializable
data class MethodMapping(
    val mcpName: String,
    val srgName: String,
    val params: String,
)