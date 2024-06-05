package ru.hollowhorizon.hc;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import ru.hollowhorizon.hc.internal.ForgeNetworkHelper;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;

@Mod("hollowcore")
public class HollowCoreForge {
    public HollowCoreForge() {
        isModLoaded = ModList.get()::isLoaded;
        ForgeNetworkHelper.register();
        var events = ForgeEvents.INSTANCE;
        var init = HollowCore.INSTANCE; // Loading Main Class
        if (FMLEnvironment.dist.isClient()) new HollowCoreClientForge();
    }
}
