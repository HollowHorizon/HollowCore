package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.nbt.Tag
import net.minecraftforge.common.capabilities.Capability

interface ICapabilityUpdater {
    fun updateCapability(capability: Capability<*>, value: Tag)
}


interface ICapabilitySyncer {
    fun onCapabilitySync(capability: Capability<*>)
}