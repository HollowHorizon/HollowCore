package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.data.Team

@HollowCapabilityV2(Team::class)
class TeamCapabilityExample: CapabilityInstance() {
    var money by syncable(10)
}