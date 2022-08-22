package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import ru.hollowhorizon.hc.common.capabilities.HollowCapability;
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityStorage;
import ru.hollowhorizon.hc.common.capabilities.HollowPlayerProvider;
import ru.hollowhorizon.hc.common.capabilities.HollowStoryCapability;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModCapabilities {
    @CapabilityInject(HollowStoryCapability.class)
    public static Capability<HollowStoryCapability> STORY_CAPABILITY = null;

    public static void init() {
        registerCapability(HollowStoryCapability.class);
    }

    public static void attachCapabilityToEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(new ResourceLocation(MODID, "player_capabilities"), new HollowPlayerProvider());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends HollowCapability<?>> void registerCapability(Class<T> tClass) {
        CapabilityManager.INSTANCE.register(
                tClass,
                (Capability.IStorage<T>) new HollowCapabilityStorage(),
                tClass::newInstance);
    }
}
