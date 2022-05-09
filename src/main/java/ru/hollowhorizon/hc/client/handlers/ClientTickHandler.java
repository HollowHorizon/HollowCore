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

//        if (Minecraft.getInstance().level != null) {
//            Minecraft.getInstance().level
//                    .entitiesForRendering()
//                    .forEach(e -> {
//                                if (!(e instanceof IAnimEntity))
//                                    return;
//
//                                ((IAnimEntity)e)
//                                        .getModel()
//                                        .getAnimator()
//                                        .onUpdate();
//                            }
//                    );
//        }
    }
}
