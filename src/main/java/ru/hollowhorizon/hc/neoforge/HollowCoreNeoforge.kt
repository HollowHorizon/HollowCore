//? if neoforge {
/*package ru.hollowhorizon.hc.neoforge

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModList
import net.neoforged.fml.common.Mod
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.fml.loading.FMLLoader
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.JavaHacks
import ru.hollowhorizon.hc.client.utils.isModLoaded
import ru.hollowhorizon.hc.client.utils.isProduction_
import ru.hollowhorizon.hc.common.registry.AutoModelType
import ru.hollowhorizon.hc.common.registry.createRegistry
import ru.hollowhorizon.hc.neoforge.internal.NeoForgeModList
import ru.hollowhorizon.hc.neoforge.internal.RegistryHolderNeoForge

@Mod("hollowcore")
class HollowCoreNeoForge(modBus: IEventBus) {
    init {
        CoreInitializationNeoForge

        MOD_BUS = modBus
        createRegistry = { resourceLocation, registry, automodel: AutoModelType?, generator, aClass: Class<*> ->
            RegistryHolderNeoForge(
                resourceLocation,
                JavaHacks.forceCast(registry),
                automodel,
                JavaHacks.forceCast(generator),
                aClass
            )
        }
        isModLoaded = ModList.get()::isLoaded
        isProduction_ = FMLLoader::isProduction

        ru.hollowhorizon.hc.client.utils.ModList.INSTANCE = NeoForgeModList

        HollowCore // Loading Main Class

        NeoForgeEvents

        if (FMLEnvironment.dist.isClient) HollowCoreClientNeoForge
    }

    companion object {
        lateinit var MOD_BUS: IEventBus
    }
}
*///?}