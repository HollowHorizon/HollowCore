//? if fabric {
package ru.hollowhorizon.hc.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.client.utils.ModList;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.fabric.internal.FabricModList;
import ru.hollowhorizon.hc.fabric.internal.NetworkHelper;
import ru.hollowhorizon.hc.fabric.internal.RegistryHolderFabric;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.*;

public class HollowCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModList.Companion.setINSTANCE(FabricModList.INSTANCE);
        isModLoaded = FabricLoader.getInstance()::isModLoaded;
        isProduction_ = () -> !FabricLoader.getInstance().isDevelopmentEnvironment();
        isPhysicalClient_ = () -> false;
        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderFabric<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));


        var core = CoreInitializationFabric.INSTANCE;
        var init = HollowCore.INSTANCE; // Loading Main Class
        HollowCore.platform = HollowCore.Platform.FABRIC;
        var events = FabricEvents.INSTANCE;

        NetworkHelper.register();
    }
}
//?}