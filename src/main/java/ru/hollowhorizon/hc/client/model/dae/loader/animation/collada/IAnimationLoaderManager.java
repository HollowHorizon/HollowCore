package ru.hollowhorizon.hc.client.model.dae.loader.animation.collada;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;

public interface IAnimationLoaderManager
{

    static IAnimationLoaderManager getInstance()
    {
        return Holder.instance;
    }

    IAnimation loadAnimation(IModel model, ResourceLocation location) throws AnimationLoadingException;

    void registerLoader(final IAnimationLoader loader);
    
    class Holder
    {
        private static IAnimationLoaderManager instance;
        
        public static void setup(final IAnimationLoaderManager manager)
        {
            instance = manager;
        }
    }
}
