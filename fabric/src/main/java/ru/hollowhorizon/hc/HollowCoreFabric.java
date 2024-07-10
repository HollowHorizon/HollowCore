package ru.hollowhorizon.hc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.internal.NetworkHelper;
import ru.hollowhorizon.hc.internal.RegistryHolderFabric;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;
import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isProduction_;

public class HollowCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        var core = CoreInitializationFabric.INSTANCE;

        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderFabric<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = FabricLoader.getInstance()::isModLoaded;
        isProduction_ = () -> !FabricLoader.getInstance().isDevelopmentEnvironment();

        var init = HollowCore.INSTANCE; // Loading Main Class
        HollowCore.platform = HollowCore.Platform.FABRIC;
        var events = FabricEvents.INSTANCE;

        TabInitializer.init()

        NetworkHelper.register();
    }
}
