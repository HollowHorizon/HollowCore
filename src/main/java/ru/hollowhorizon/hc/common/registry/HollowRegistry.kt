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

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.rl
import kotlin.properties.ReadOnlyProperty

open class HollowRegistry(val modId: String = MODID) {
    /**
     * Avoid fake NotNulls parameters like BlockEntityType.Builder::build
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> promise(): T = null as T

    inline fun <reified T : Any> register(
        location: ResourceLocation,
        autoModel: AutoModelType? = AutoModelType.DEFAULT,
        registry: Registry<in T>? = null,
        noinline registryEntry: () -> T,
    ): IRegistryHolder<T> {
        REGISTRIES.entries.firstOrNull { it.key.isAssignableFrom(T::class.java) }?.let {
            val coreRegistry = it.value as CoreRegistry<T>
            val data by lazy { registryEntry() }
            val entry = RegistryObject { data }
            coreRegistry[location] = entry
            return IRegistryHolder { _, _ -> entry }
        }
        return createRegistry(location, registry, autoModel, registryEntry, T::class.java) as IRegistryHolder<T>
    }

    inline fun <reified T : Any> register(
        id: String,
        autoModel: AutoModelType? = AutoModelType.DEFAULT,
        registry: Registry<in T>? = null,
        noinline registryEntry: () -> T,
    ): IRegistryHolder<T> = register("$modId:$id".rl, autoModel, registry, registryEntry)
}

open class CoreRegistry<T>(val registryName: ResourceLocation) {

    private val entries: MutableMap<ResourceLocation, RegistryObject<T>> = Object2ObjectOpenHashMap()


    operator fun set(key: ResourceLocation, value: RegistryObject<T>) {
        entries[key] = value
    }
    operator fun get(id: ResourceLocation): T =
        entries[id]?.get() ?: throw IllegalStateException("Element $id not found in registry $registryName")

    operator fun get(value: T): ResourceLocation = entries.entries.first { it.value == value }.key

    operator fun contains(id: ResourceLocation): Boolean = entries.keys.any { it == id }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Registry

val REGISTRIES = Object2ObjectOpenHashMap<Class<*>, CoreRegistry<*>>()

lateinit var createRegistry: (ResourceLocation, Registry<*>?, AutoModelType?, () -> Any, Class<*>) -> IRegistryHolder<*>

fun interface IRegistryHolder<T> : ReadOnlyProperty<Any?, RegistryObject<T>>

fun interface RegistryObject<T> {
    fun get(): T
}