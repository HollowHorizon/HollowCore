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

@file:Suppress("UNCHECKED_CAST")

package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.data.TeamBase
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModList
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
annotation class HollowCapabilityV2(vararg val value: KClass<*>) {
    companion object {

        fun <T> get(clazz: Class<T>): Capability<T> {
            return CapabilityStorage.storages[clazz.name] as Capability<T>
        }

        @JvmField
        val TYPE: Type = Type.getType(HollowCapabilityV2::class.java)

    }
}

fun <T> callHook(list: List<ModFileScanData>, getMethod: (String, Boolean) -> Capability<T>) {
    val data = list.flatMap { it.annotations }
    val annotations = data
        .filter { HollowCapabilityV2.TYPE == it.annotationType }
        .distinct()
        .sortedBy { it.clazz.toString() }

    for (annotation in annotations) {

        HollowCore.LOGGER.debug(Logging.CAPABILITIES, "Attempting to automatically register: {}", annotation)
        val result = getMethod(
            annotation.clazz.internalName
                .replace("/", ".")
                .replace("$", "."), true
        )

        val targets: List<Type> =
            (annotation.annotationData["value"] as ArrayList<Type>)
        initCapabilities(Class.forName(annotation.clazz.className), result, targets)
    }
}

fun initCapabilities(capabilityClass: Class<*>, cap: Capability<*>, targets: List<Type>) {
    CapabilityStorage.storages[cap.name] = cap

    targets.forEach { target ->
        if(target.className.contains("ftbteams") && !ModList.get().isLoaded("ftbteams")) return@forEach

        val targetClass = Class.forName(target.className)

        if (targetClass == Player::class.java) CapabilityStorage.playerCapabilities.add(cap)
        if (targetClass == Level::class.java) CapabilityStorage.levelCapabilities.add(cap)
        else if (ModList.get().isLoaded("ftbteams") && targetClass == TeamBase::class.java) CapabilityStorage.teamCapabilities.add(cap)

        CapabilityStorage.providers.add(targetClass to { provider ->
            (capabilityClass.getDeclaredConstructor().newInstance() as CapabilityInstance).apply {
                this.provider = provider
                this.capability = cap as Capability<CapabilityInstance>
            }
        })
    }
}
