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
import ru.hollowhorizon.hc.HollowCore
import java.io.InputStream

object HollowMappings {
    @JvmField
    val MAPPINGS = Mappings.loadFromTiny(HollowCore::class.java.getResourceAsStream("/mappings.tiny")!!)
}

fun main() {
    val mapping = HollowMappings.MAPPINGS

    println(mapping.mappings.find { it.mcpName == "net.minecraft.client.Minecraft" }?.methodObf("getInstance", "()Lnet/minecraft/class_310;"))
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

        fun loadFromTiny(stream: InputStream): Mappings {
            val mappings = HashSet<ClassMapping>()
            var lastClass: ClassMapping? = null
            stream.bufferedReader().forEachLine { text ->
                val line = text.trim('\t', ' ')
                when {
                    // Парсинг класса
                    line.startsWith("c") -> {
                        if (text.startsWith('\t')) return@forEachLine
                        val data = line.replace("/", ".").replace("$", ".").split('\t', ' ')
                        mappings.add(ClassMapping(data[2], data[3], data[1]).apply {
                            lastClass = this
                        })
                    }

                    line.startsWith("m") -> { //Если идёт метод или параметр
                        val data = line.replace("/", ".").replace("$", ".").split('\t', ' ')
                        lastClass?.methods?.add(MethodMapping(data[4], data[3], data[1]))
                    }

                    line.startsWith("f") -> { //Если идёт метод или параметр
                        try {
                            val data = line.replace("/", ".").replace("$", ".").split('\t', ' ')
                            lastClass?.fields?.add(FieldMapping(data[4], data[3]))
                        } catch (e: Exception) {
                            throw e
                        }
                    }
                }
            }
            stream.close()

            remapParams(mappings)
            return Mappings(mappings)
        }

        private fun remapParams(mappings: HashSet<ClassMapping>) {
            val names = mappings.associate { it.obfName to it.srgName }
            mappings.forEach { mapping ->
                mapping.methods.forEach {
                    it.params = remapParameter(it.params, names)
                }
            }
        }

        fun remapParameter(params: String, names: Map<String, String>): String {
            val classPattern = "L([a-zA-Z0-9/_]+);".toRegex()

            return classPattern.replace(params) { matchResult ->
                val className = matchResult.groupValues[1]
                val newClassName = (names[className] ?: className).replace('.', '/').replace('$', '/')
                "L$newClassName;"
            }
        }
    }

    @kotlinx.serialization.Transient
    val fields = mappings.flatMap { it.fields.map { f -> f.srgName to f.mcpName } }.toMap()

    @kotlinx.serialization.Transient
    val methods = mappings.flatMap { it.methods.map { m -> m.srgName to m.mcpName } }.toMap()

    @kotlinx.serialization.Transient
    val classes = mappings.associate { it.srgName to it.mcpName }

    @kotlinx.serialization.Transient
    val mcpToSrg = mappings.associate { it.mcpName to it.srgName }

    operator fun get(node: ClassNode): ClassMapping? {
        val name = node.name.replace("/", ".").replace("$", ".")
        return mappings.find { it.mcpName == name || it.srgName == name }
    }

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

    fun classObf(name: String): String {
        val deobfName = name.replace("/", ".").replace("$", ".")
        return this.mappings.find { it.mcpName == deobfName }?.srgName
            ?.replace('.', '/')?.replace('$', '.')
            ?: name
    }

    fun classObf(node: ClassNode) {
        val isSubClass = node.superName.contains('$')
        val name = node.superName.replace("/", ".").replace("$", ".")
        node.superName = this.mappings.find { it.mcpName == name }?.srgName?.replace('.', '/')?.let { className ->
            if (isSubClass) {
                val i = className.indexOfLast { it == '$' }
                className.substring(0, i - 1) + '$' + className.substring(i)
                ""
            } else className
        } ?: return
    }

    fun classDeobf(name: String) = this.classes.getOrDefault(name, name)
    fun fieldDeobf(name: String) = this.fields.getOrDefault(name, name)
    fun methodDeobf(name: String) = this.methods.getOrDefault(name, name)

    fun descObf(desc: String): String {
        val classPattern = "L([a-zA-Z0-9/_]+);".toRegex()

        return classPattern.replace(desc) { matchResult ->
            val className = matchResult.groupValues[1]
            val newClassName = mcpToSrg[className] ?: className
            "L$newClassName;"
        }
    }
}

@Serializable
data class ClassMapping(
    val srgName: String,
    val mcpName: String,
    var obfName: String = "",
) {
    val fields = HashSet<FieldMapping>()
    val methods = HashSet<MethodMapping>()

    fun fieldObf(text: String): String {
        return fields.find { it.mcpName == text }?.srgName ?: text
    }

    fun methodObf(deobfName: String, signature: String): String {
        return methods.firstOrNull {
            it.mcpName == deobfName && it.params == signature
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
    var params: String,
)