package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorageV2;

public class ModCapabilities {

    public static void init() {
        HollowCapabilityStorageV2.INSTANCE.registerAll();
    }

    public static void attachCapabilityToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            //event.addCapability(new ResourceLocation(MODID, "player_capabilities"), new HollowPlayerProvider());
        }
        if (event.getObject() instanceof IAnimatedEntity) {
            //event.addCapability(new ResourceLocation(MODID, "animated_entity"), new AnimatedEntityProvider());
        }
    }


}
