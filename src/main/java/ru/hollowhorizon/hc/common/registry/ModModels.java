package ru.hollowhorizon.hc.common.registry;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.collada.IModelLoaderManager;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModModels {

    public static IModel TEST = IModelLoaderManager.getInstance().loadModel(
            new ResourceLocation(MODID, "models/untitled.dae"),
            new ResourceLocation(MODID, "textures/entity/diffuse.png")
    );
}
