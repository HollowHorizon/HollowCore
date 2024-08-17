//? if fabric {
package ru.hollowhorizon.hc.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import ru.hollowhorizon.hc.client.HollowCoreClient;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;
import ru.hollowhorizon.hc.fabric.internal.IrisHelper;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isPhysicalClient_;
import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.shouldOverrideShaders;

public class HollowCoreClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isModLoaded("iris")) { //TODO: FixMe
            ForgeKotlinKt.areShadersEnabled_ = () -> false; //IrisApi.getInstance().getConfig()::areShadersEnabled;
            shouldOverrideShaders = IrisHelper::shouldOverrideShaders;
        } else {
            ForgeKotlinKt.areShadersEnabled_ = () -> false;
            shouldOverrideShaders = () -> false;
        }

        isPhysicalClient_ = () -> true;
        var events = FabricClientEvents.INSTANCE;
        var init = HollowCoreClient.INSTANCE; // Loading Main Class
    }
}
//?}