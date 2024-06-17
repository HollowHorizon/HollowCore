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
import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.isProduction_;

//TODO: Переорганизовать все эти классы, оставить только Kotlin и сделать более понятную инициализацию.
public class HollowCoreFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        var core = CoreInitializationFabric.INSTANCE;

        HollowRegistryKt.createRegistry = ((resourceLocation, registry, aBoolean, function0, aClass) -> new RegistryHolderFabric<>(resourceLocation, JavaHacks.forceCast(registry), aBoolean, JavaHacks.forceCast(function0), aClass));
        isModLoaded = FabricLoader.getInstance()::isModLoaded;
        isProduction_ = () -> !FabricLoader.getInstance().isDevelopmentEnvironment();

        var init = HollowCore.INSTANCE; // Loading Main Class
        var events = FabricEvents.INSTANCE;

        NetworkHelper.register();

        ServerTickEvents.END_SERVER_TICK.register(s -> {
            EventBus.post(new TickEvent.Server(s));
        });
    }
}
