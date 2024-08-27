//? if forge {
package ru.hollowhorizon.hc.forge;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.forge.internal.ForgeNetworkHelper;
import ru.hollowhorizon.hc.forge.internal.RegistryHolderForge;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.*;

@Mod("hollowcore")
public class HollowCoreForge {
    public HollowCoreForge() {
        ru.hollowhorizon.hc.client.utils.ModList.Companion.setINSTANCE(ForgeModList.INSTANCE);
        var core = CoreInitializationForge.INSTANCE;

        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderForge<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = ModList.get()::isLoaded;
        isProduction_ = FMLLoader::isProduction;
        shouldOverrideShaders = () -> false;

        var events = ForgeEvents.INSTANCE;
        var init = HollowCore.INSTANCE; // Loading Main Class
        HollowCore.platform = HollowCore.Platform.FORGE;

        ForgeNetworkHelper.register();

        if (FMLEnvironment.dist.isClient()) new HollowCoreClientForge();
    }
}
//?}