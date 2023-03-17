package ru.hollowhorizon.hc.client.models.core;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.animation.BTAdditionalAnimationEntry;
import ru.hollowhorizon.hc.client.models.core.materials.BTMaterialEntry;
import ru.hollowhorizon.hc.client.models.core.model.BTArmorModelEntry;
import ru.hollowhorizon.hc.client.models.core.model.BTModel;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BoneTownRegistry {

    public static IForgeRegistry<BTModel> MODEL_REGISTRY = null;
    public static IForgeRegistry<BTMaterialEntry> MATERIAL_REGISTRY = null;
    public static IForgeRegistry<BTAdditionalAnimationEntry> ADDITIONAL_ANIMATION_REGISTRY = null;
    public static IForgeRegistry<BTArmorModelEntry> ARMOR_MODEL_REGISTRY = null;

    @SuppressWarnings("unused")
    public static void createRegistries(RegistryEvent.NewRegistry event) {
        HollowCore.LOGGER.info("Registering Bone Town Registries");
        MODEL_REGISTRY = new RegistryBuilder<BTModel>()
                .setName(new ResourceLocation(HollowCore.MODID, "models"))
                .setType(BTModel.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .allowModification()
                .create();
        MATERIAL_REGISTRY = new RegistryBuilder<BTMaterialEntry>()
                .setName(new ResourceLocation(HollowCore.MODID, "materials"))
                .setType(BTMaterialEntry.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .allowModification()
                .create();
        ADDITIONAL_ANIMATION_REGISTRY = new RegistryBuilder<BTAdditionalAnimationEntry>()
                .setName(new ResourceLocation(HollowCore.MODID, "z_add_animations"))
                .setType(BTAdditionalAnimationEntry.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .allowModification()
                .create();
        ARMOR_MODEL_REGISTRY = new RegistryBuilder<BTArmorModelEntry>()
                .setName(new ResourceLocation(HollowCore.MODID, "z_armor_models"))
                .setType(BTArmorModelEntry.class)
                .setIDRange(0, Integer.MAX_VALUE - 1)
                .allowModification()
                .create();
    }
}
