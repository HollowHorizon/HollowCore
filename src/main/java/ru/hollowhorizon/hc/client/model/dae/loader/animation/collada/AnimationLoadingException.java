package ru.hollowhorizon.hc.client.model.dae.loader.animation.collada;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class AnimationLoadingException extends Exception
{

    public AnimationLoadingException(@Nonnull final Class<? extends IAnimationLoader> loaderClass, @Nonnull final ResourceLocation location, @Nonnull final Throwable cause)
    {
        super("Failed to load Animation: " + location + " by: " + loaderClass.getName(), cause);
    }
}
