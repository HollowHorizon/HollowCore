package ru.hollowhorizon.hc;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.internal.RegistryHolderNeoForge;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;

@Mod("hollowcore")
public class HollowCoreNeoForge {
    public static IEventBus MOD_BUS;

    public HollowCoreNeoForge(IEventBus modBus) {
        MOD_BUS = modBus;
        isModLoaded = ModList.get()::isLoaded;
        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderNeoForge<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        var events = NeoForgeEvents.INSTANCE;
        var init = HollowCore.INSTANCE; // Loading Main Class
        if (FMLEnvironment.dist.isClient()) new HollowCoreClientNeoForge();
    }
}
