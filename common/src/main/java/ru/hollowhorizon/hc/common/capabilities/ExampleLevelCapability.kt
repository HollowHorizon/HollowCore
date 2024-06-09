package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.world.level.Level

@HollowCapabilityV2(Level::class)
class ExampleLevelCapability : CapabilityInstance() {
    var money by syncable(50)
}