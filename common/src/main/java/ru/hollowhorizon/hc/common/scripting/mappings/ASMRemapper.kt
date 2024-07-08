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

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.*
import ru.hollowhorizon.hc.HollowCore
import java.io.ByteArrayInputStream

object ASMRemapper {
    @JvmField
    val CLASS_CACHE = HashMap<String, ByteArray>()

    fun remap(bytes: ByteArray): ByteArray {
        val node = bytes.readClass()

        remapMethods(node)
        remapFields(node)

        HollowMappings.MAPPINGS.classObf(node)

        return node.writeClass()
    }

    private fun remapMethods(node: ClassNode) {
        node.methods.forEach { method ->
            remapMethod(node, method)
            method.instructions.forEach { insn ->
                when (insn) {
                    is FieldInsnNode -> {
                        val owner = insn.owner.node
                        if (owner != null) {
                            remapFieldInsn(owner, insn)
                            insn.owner = HollowMappings.MAPPINGS.classObf(insn.owner)
                            insn.desc = HollowMappings.MAPPINGS.descObf(insn.desc)
                        }
                    }

                    is MethodInsnNode -> {
                        val owner = insn.owner.node

                        if (owner != null) {
                            remapMethodInsn(owner, insn)
                            insn.owner = HollowMappings.MAPPINGS.classObf(insn.owner)
                            insn.desc = HollowMappings.MAPPINGS.descObf(insn.desc)
                        }
                    }
                }
            }
        }
    }

    private fun remapFieldInsn(node: ClassNode, field: FieldInsnNode) {
        field.name = HollowMappings.MAPPINGS.fieldObf(node, field.name)
    }

    private fun remapMethod(node: ClassNode, method: MethodNode) {
        method.name = HollowMappings.MAPPINGS.methodObf(node, method.name, method.desc)
    }

    private fun remapMethodInsn(node: ClassNode, method: MethodInsnNode) {
        method.name = HollowMappings.MAPPINGS.methodObf(node, method.name, method.desc)
    }

    private fun remapFields(node: ClassNode) {
        node.fields.forEach { method ->
            remapField(node, method)
        }
    }

    private fun remapField(node: ClassNode, field: FieldNode) {
        field.name = HollowMappings.MAPPINGS.fieldObf(node, field.name)
    }


}

fun ByteArray.readClass(): ClassNode {
    val classNode = ClassNode()
    val classReader = ClassReader(this)
    classReader.accept(classNode, 0)
    return classNode
}

fun ClassNode.writeClass(): ByteArray {
    val classWriter = ClassWriter(0)
    this.accept(classWriter)
    return classWriter.toByteArray()
}

val String.node: ClassNode?
    get() {
        return try {
            val node = ClassNode()
            node.apply {
                val clazz = this@node.replace(".", "/") + ".class"
                val stream =
                    if (ASMRemapper.CLASS_CACHE.containsKey(clazz)) ByteArrayInputStream(ASMRemapper.CLASS_CACHE[clazz])
                    else Thread.currentThread().contextClassLoader.getResourceAsStream(clazz)
                        ?: Class.forName(this@node.replace("/", ".")).classLoader.getResourceAsStream(clazz)

                if (stream == null) HollowCore.LOGGER.warn(
                    "Class {} not found! It may be classLoader: {}",
                    this@node.replace("/", "."),
                    Class.forName(this@node.replace("/", ".")).classLoader.name
                )
                ClassReader(stream).accept(this, 0)
                stream?.close()
            }
        } catch (e: Exception) {
            HollowCore.LOGGER.error("error, not found: {}", this, e)
            null
        }
    }