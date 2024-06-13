package ru.hollowhorizon.hc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import ru.hollowhorizon.hc.client.HollowCoreClient;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.registry.RegisterKeyBindingsEvent;
import ru.hollowhorizon.hc.common.events.tick.TickEvent;

public class HollowCoreClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        var events = FabricClientEvents.INSTANCE;
        var init = HollowCoreClient.INSTANCE; // Loading Main Class
    }
}
