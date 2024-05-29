package ru.hollowhorizon.hc;

import kotlin.Unit;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.loader.api.FabricLoader;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;

public class HollowCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        isModLoaded = FabricLoader.getInstance()::isModLoaded;
        HollowCoreEvents.registerAttributesEvent = (a, b) -> {
            FabricDefaultAttributeRegistry.register(a, b);
            return Unit.INSTANCE;
        };
        var init = HollowCore.INSTANCE; // Loading Main Class
    }
}
