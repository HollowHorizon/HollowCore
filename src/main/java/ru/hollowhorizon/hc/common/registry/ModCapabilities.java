package ru.hollowhorizon.hc.common.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;

public class ModCapabilities {

    public static void init() {
        HollowCapabilityStorageV2.INSTANCE.registerAll();
    }

    public static void attachCapabilityToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            //event.addCapability(new ResourceLocation(MODID, "player_capabilities"), new HollowPlayerProvider());
        }

    }


}
