package ru.hollowhorizon.hc;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import ru.hollowhorizon.hc.client.utils.JavaHacks;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.tick.TickEvent;
import ru.hollowhorizon.hc.common.registry.HollowRegistryKt;
import ru.hollowhorizon.hc.internal.NetworkHelper;
import ru.hollowhorizon.hc.internal.RegistryHolderFabric;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isModLoaded;

public class HollowCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        HollowRegistryKt.createRegistry = ((resourceLocation, aBoolean, function0, aClass) -> new RegistryHolderFabric<>(resourceLocation, aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = FabricLoader.getInstance()::isModLoaded;
        NetworkHelper.register();
        var init = HollowCore.INSTANCE; // Loading Main Class
        var events = FabricEvents.INSTANCE;

        ServerTickEvents.END_SERVER_TICK.register(s -> {
            EventBus.post(new TickEvent.Server(s));
        });
    }
}
