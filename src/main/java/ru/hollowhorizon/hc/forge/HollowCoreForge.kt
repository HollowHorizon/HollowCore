//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.fml.common.Mod
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.HollowCoreClient
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.isPhysicalClient
import ru.hollowhorizon.hc.common.registry.createRegistry
import ru.hollowhorizon.hc.forge.internal.ForgeNetworkHelper
import ru.hollowhorizon.hc.forge.internal.RegistryHolderForge

@Mod("hollowcore")
class HollowCoreForge {
    init {
        commonInit()

        if (isPhysicalClient) clientInit()
    }

    private fun commonInit() {
        createRegistry =
            { location, registry, modelType, value, type ->
                RegistryHolderForge(
                    location,
                    JavaHacks.forceCast(registry),
                    modelType,
                    JavaHacks.forceCast(value),
                    type
                )
            }

        CoreInitializationForge
        ForgeEvents
        HollowCore // Loading Main Class

        ForgeNetworkHelper.register()
    }

    private fun clientInit() {
        ForgeClientEvents
        HollowCoreClient
    }
} *///?}
