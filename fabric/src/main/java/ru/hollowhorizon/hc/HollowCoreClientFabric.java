package ru.hollowhorizon.hc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.irisshaders.iris.api.v0.IrisApi;
import ru.hollowhorizon.hc.client.HollowCoreClient;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;

public class HollowCoreClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) {
            ForgeKotlinKt.areShadersEnabled_ = IrisApi.getInstance().getConfig()::areShadersEnabled;
        } else {
            ForgeKotlinKt.areShadersEnabled_ = () -> false;
        }

        var events = FabricClientEvents.INSTANCE;
        var init = HollowCoreClient.INSTANCE; // Loading Main Class
    }
}
