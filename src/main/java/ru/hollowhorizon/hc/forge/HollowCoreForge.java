//? if forge {
/*package ru.hollowhorizon.hc.forge;

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

        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderForge<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = ModList.get()::isLoaded;
        isProduction_ = // На 1.20+ Forge почему-то решил больше не обфусцировать игру... Ну и зачем я тогда обфускатор скриптов писал?((
                //? if <=1.20.1 {
                /^FMLLoader::isProduction;
                ^///?} else {
                () -> false;
                //?}
        shouldOverrideShaders = () -> false;

        var core = CoreInitializationForge.INSTANCE;
        var events = ForgeEvents.INSTANCE;
        var init = HollowCore.INSTANCE; // Loading Main Class

        GameRemapper.INSTANCE.remap();
        ForgeNetworkHelper.register();

        if (FMLEnvironment.dist.isClient()) new HollowCoreClientForge();
    }
}
*///?}