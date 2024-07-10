package ru.hollowhorizon.hc;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.internal.RegistryHolderNeoForge;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;
import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isProduction_;

@Mod("hollowcore")
public class HollowCoreNeoForge {
    public static IEventBus MOD_BUS;

    public HollowCoreNeoForge(IEventBus modBus) {
        var core = CoreInitializationNeoForge.INSTANCE;

        MOD_BUS = modBus;
        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderNeoForge<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = ModList.get()::isLoaded;
        isProduction_ = FMLLoader::isProduction;

        var init = HollowCore.INSTANCE; // Loading Main Class
        HollowCore.platform = HollowCore.Platform.NEOFORGE;
        var events = NeoForgeEvents.INSTANCE;

        TabInitializer.init()

        if (FMLEnvironment.dist.isClient()) new HollowCoreClientNeoForge();
    }
}
