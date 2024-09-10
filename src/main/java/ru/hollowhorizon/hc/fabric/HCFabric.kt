//? if fabric {
package ru.hollowhorizon.hc.fabric

import net.fabricmc.loader.api.FabricLoader
import net.irisshaders.iris.api.v0.IrisApi
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.HollowCoreClient
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.ModList
import ru.hollowhorizon.hc.client.utils.areShadersEnabled_
import ru.hollowhorizon.hc.client.utils.isModLoaded
import ru.hollowhorizon.hc.client.utils.isPhysicalClient_
import ru.hollowhorizon.hc.client.utils.isProduction_
import ru.hollowhorizon.hc.client.utils.shouldOverrideShaders
import ru.hollowhorizon.hc.common.registry.createRegistry
import ru.hollowhorizon.hc.fabric.internal.FabricModList
import ru.hollowhorizon.hc.fabric.internal.IrisHelper
import ru.hollowhorizon.hc.fabric.internal.NetworkHelper
import ru.hollowhorizon.hc.fabric.internal.RegistryHolderFabric

object HCFabric {
    @JvmStatic
    fun onCommonInitialize() {
        ModList.INSTANCE = FabricModList
        isModLoaded = FabricLoader.getInstance()::isModLoaded
        isProduction_ = { !FabricLoader.getInstance().isDevelopmentEnvironment }
        isPhysicalClient_ = { false }
        createRegistry = { rl, reg, bool, f, a ->
            RegistryHolderFabric(rl, JavaHacks.forceCast(reg), bool, JavaHacks.forceCast(f), a)
        }

        CoreInitializationFabric
        HollowCore
        GameRemapper.remap()
        FabricEvents

        NetworkHelper.register()
    }

    @JvmStatic
    fun onClientInitialize() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            areShadersEnabled_ = IrisApi.getInstance().config::areShadersEnabled
            shouldOverrideShaders = IrisHelper::shouldOverrideShaders
        } else {
            areShadersEnabled_ = { false }
            shouldOverrideShaders = { false }
        }

        isPhysicalClient_ = { true }
        FabricClientEvents
        HollowCoreClient
    }
}
//?}