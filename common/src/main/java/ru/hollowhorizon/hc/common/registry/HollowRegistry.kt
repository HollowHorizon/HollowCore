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

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore
import kotlin.properties.ReadOnlyProperty

open class HollowRegistry {

    //Avoid fake NotNulls parameters like BlockEntityType.Builder::build
    @Suppress("UNCHECKED_CAST")
    fun <T> promise(): T = null as T

    inline fun <reified T : Any> register(
        location: ResourceLocation,
        autoModel: Boolean = true,
        registry: Registry<in T>? = null,
        noinline registryEntry: () -> T,
    ): IRegistryHolder<T> {
        HollowCore.LOGGER.info("Registering: {}", location)
        return createRegistry(location, registry, autoModel, registryEntry, T::class.java) as IRegistryHolder<T>
    }

    
}

lateinit var createRegistry: (ResourceLocation, Registry<*>?, Boolean, () -> Any, Class<*>) -> IRegistryHolder<*>

interface IRegistryHolder<T> : ReadOnlyProperty<Any?, RegistryObject<T>>

fun interface RegistryObject<T> {
    fun get(): T
}