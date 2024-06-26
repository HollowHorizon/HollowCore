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

package ru.hollowhorizon.hc.common

import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraftforge.event.entity.EntityAttributeCreationEvent
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage
import ru.hollowhorizon.hc.common.commands.HollowCommands
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler
import ru.hollowhorizon.hc.common.network.NetworkHandler
import ru.hollowhorizon.hc.common.registry.ModEntities
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.addGenericListener

object HollowCoreCommon {
    init {
        MOD_BUS.addListener(::onSetup)
        MOD_BUS.addListener(::onRegisterAttributes)
        FORGE_BUS.addListener(HollowCommands::onRegisterCommands)

        FORGE_BUS.register(HollowEventHandler)

        FORGE_BUS.addGenericListener(CapabilityStorage::registerProvidersEntity)
        FORGE_BUS.addGenericListener(CapabilityStorage::registerProvidersBlockEntity)
        FORGE_BUS.addGenericListener(CapabilityStorage::registerProvidersWorld)
        if (ModList.get().isLoaded("ftbteams")) FORGE_BUS.addGenericListener(CapabilityStorage::registerProvidersTeam)
    }

    private fun onSetup(event: FMLCommonSetupEvent) {
        NetworkHandler.register()
    }

    private fun onRegisterAttributes(event: EntityAttributeCreationEvent) {
        event.put(ModEntities.TEST_ENTITY.get(), Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.2).build())
    }
}