package ru.hollowhorizon.hc.client.model.dae.loader.model.collada;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimatedModelData;

import javax.annotation.Nonnull;

public interface IModelLoader {
    boolean canLoadModel(@Nonnull ResourceLocation modelLocation);

    AnimatedModelData loadModel(ResourceLocation colladaFile) throws ModelLoadingException;
}
