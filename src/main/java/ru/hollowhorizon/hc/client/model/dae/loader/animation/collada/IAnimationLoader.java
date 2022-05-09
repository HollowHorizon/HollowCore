package ru.hollowhorizon.hc.client.model.dae.loader.animation.collada;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimationData;

public interface IAnimationLoader
{
    boolean canLoadAnimation(ResourceLocation animationLocation);

    AnimationData loadAnimation(ResourceLocation animationLocation) throws AnimationLoadingException;
}
