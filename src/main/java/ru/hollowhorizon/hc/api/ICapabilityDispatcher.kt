package ru.hollowhorizon.hc.api

import net.minecraftforge.common.capabilities.CapabilityDispatcher

interface ICapabilityDispatcher {
    val capabilities: CapabilityDispatcher?
}