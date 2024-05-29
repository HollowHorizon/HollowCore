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
package ru.hollowhorizon.hc.common.handlers

object HollowEventHandler { //TODO: Fix
//    @OnlyIn(Dist.CLIENT)
//    fun onTooltip(event: ItemTooltipEvent) {
//        val desc = event.itemStack.item.descriptionId + ".hc_desc"
//        val shiftDesc = event.itemStack.item.descriptionId + ".hc_shift_desc"
//        val lang = Language.getInstance()
//
//        if (lang.has(desc)) event.toolTip.add(Component.translatable(desc))
//
//        if (Screen.hasShiftDown() && lang.has(shiftDesc)) event.toolTip.add(Component.translatable(desc))
//    }
//
//    fun onStartTracking(event: StartTracking) {
//        providers.filter { it.first.isInstance(event.target) }.forEach {
//            event.target
//                .getCapability(it.second.invoke(event.target).capability)
//                .ifPresent(CapabilityInstance::sync)
//        }
//    }
//
//    fun onPlayerClone(event: Clone) {
//        if (event.isWasDeath) {
//            for (cap in getCapabilitiesForPlayer()) {
//                val origCap = event.original.getCapability(cap)
//                if (!origCap.isPresent) continue
//                val newCap = event.entity.getCapability(cap)
//                    .orElseThrow { IllegalStateException("Capability not present!") } as CapabilityInstance
//
//                origCap.ifPresent { newCap.deserializeNBT(it.serializeNBT()) }
//            }
//        }
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    fun onPlayerLoggedIn(event: PlayerLoggedInEvent) {
//        val player = event.entity as ServerPlayer
//
//        for (cap in getCapabilitiesForPlayer()) player.getCapability(cap).ifPresent(CapabilityInstance::sync)
//        for (cap in getCapabilitiesForLevel()) player.level.getCapability(cap).ifPresent(CapabilityInstance::sync)
//
//        if (ModList.get().isLoaded("ftbteams")) {
//            for (cap in teamCapabilities) (FTBTeamsAPI.getPlayerTeam(player) as ICapabilityProvider)
//                .getCapability(cap as Capability<CapabilityInstance>).ifPresent(CapabilityInstance::sync)
//        }
//    }
//
//    fun onPlayerChangeDimension(event: PlayerChangedDimensionEvent) {
//        for (cap in getCapabilitiesForLevel()) {
//            event.entity.server?.getLevel(event.to)?.getCapability(cap)?.ifPresent(CapabilityInstance::sync)
//        }
//    }
}
