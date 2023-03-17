package ru.hollowhorizon.hc.common.capabilities.provider

import net.minecraftforge.common.capabilities.Capability
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.capabilities.initCapability
import java.util.function.Function

class CapabilityBuilder(val classes: ArrayList<Type>) : Function<Capability<*>, Any?> {
    override fun apply(capability: Capability<*>): Any? {
        initCapability(capability, classes)
        return null
    }
}