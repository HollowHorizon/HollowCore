package ru.hollowhorizon.hc.client.model.dae.loader.model.collada;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;

public interface IModelLoaderManager
{

    static IModelLoaderManager getInstance()
    {
        return Holder.instance;
    }

    IModel loadModel(final ResourceLocation location, final ResourceLocation texture);

    void registerLoader(final IModelLoader loader);

    class Holder
    {
        private static IModelLoaderManager instance;

        public static void setup(final IModelLoaderManager manager)
        {
            instance = manager;
        }
    }
}
