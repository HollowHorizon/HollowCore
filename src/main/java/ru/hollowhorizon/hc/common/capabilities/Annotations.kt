package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.nbt.INBT
import net.minecraftforge.common.capabilities.Capability

interface ICapabilityUpdater {
    fun updateCapability(capability: Capability<*>, value: INBT)
}


interface ICapabilitySyncer {
    fun onCapabilitySync(capability: Capability<*>)
}