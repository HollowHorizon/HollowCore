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

package ru.hollowhorizon.hc.common.registry

import org.reflections.Reflections
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.utils.nbt.NBT_TAGS
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.network.register
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier


object HollowModProcessor {
    private var isInitialized = false

    @JvmStatic
    fun initMod() {
        if (isInitialized) return
        isInitialized = true
        registerHandler<HollowPacketV2> { type, _ ->
            if (HollowPacketV3::class.java in type.interfaces) type.register("hollowcore")
            else HollowCore.LOGGER.warn("Unsupported packet: ${type.simpleName}")
        }

        registerHandler<Polymorphic> { type, annotation ->
            NBT_TAGS.computeIfAbsent(annotation.baseClass) { ArrayList() }.add(type.kotlin)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private inline fun <reified T : Annotation> registerHandler(noinline task: (Class<*>, T) -> Unit) {
        Reflections("ru.hollowhorizon").getTypesAnnotatedWith(T::class.java).parallelStream().forEach {
            val annotation = it.getAnnotation(T::class.java)
            task(it, annotation)
        }
    }
}

private fun Field.isStatic(): Boolean {
    return Modifier.isStatic(this.modifiers)
}

private fun Method.isStatic(): Boolean {
    return Modifier.isStatic(this.modifiers)
}

class AnnotationContainer<T : Any>(
    val modId: String,
    val annotation: T,
    val targetName: String,
) {
    var whenPropertyTask: (Field) -> Unit = {}
    var whenClassTask: (Class<*>) -> Unit = {}
}

