package ru.hollowhorizon.hc.client.model.dae.loader.model.collada;

import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

/**
 * Signals an error during the loading of a Animatrix Model.
 */
public class ModelLoadingException extends RuntimeException {

    public ModelLoadingException(@Nonnull final Class<? extends IModelLoader> loaderClass, @Nonnull final ResourceLocation location, @Nonnull final Throwable cause) {
        super("Failed to load Model: " + location + " by: " + loaderClass.getName(), cause);
    }
}
