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

import dev.ftb.mods.ftbteams.data.Team
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import ru.hollowhorizon.hc.client.utils.rl

object CapabilityStorage {
    val storages = hashMapOf<String, Capability<*>>()
    val playerCapabilities = arrayListOf<Capability<*>>()
    val teamCapabilities = arrayListOf<Capability<*>>()
    val providers = hashSetOf<Pair<Class<*>, (ICapabilityProvider) -> CapabilityInstance>>()

    fun getCapabilitiesForPlayer(): List<Capability<CapabilityInstance>> {
        return playerCapabilities as List<Capability<CapabilityInstance>>
    }

    @JvmStatic
    fun <T : CapabilityInstance> getCapability(cap: Class<T>): Capability<T> {
        return storages[cap.name] as Capability<T>
    }

    @JvmStatic
    fun registerProvidersEntity(event: AttachCapabilitiesEvent<Entity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersBlockEntity(event: AttachCapabilitiesEvent<BlockEntity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<Level>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersTeam(event: AttachCapabilitiesEvent<Team>) = event.initCapabilities()


    private fun <T> AttachCapabilitiesEvent<T>.initCapabilities() {
        providers.filter { it.first.isInstance(this.`object`) }.forEach {
            val inst = it.second(this.`object` as ICapabilityProvider)
            this.addCapability(inst.capability.createName(), inst)
        }
    }

    private fun Capability<*>.createName(): ResourceLocation {
        return "hc_capabilities:${this.name.lowercase().replace(Regex("[^a-z0-9/._-]"), "")}".rl
    }
}