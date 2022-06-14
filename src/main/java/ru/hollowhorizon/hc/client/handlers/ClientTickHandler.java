package ru.hollowhorizon.hc.client.handlers;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;

public class ClientTickHandler {
    public static int ticksInGame = 0;

    public static void clientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if (!Minecraft.getInstance().isPaused()) {
                ticksInGame++;
            }
        }
    }
}
