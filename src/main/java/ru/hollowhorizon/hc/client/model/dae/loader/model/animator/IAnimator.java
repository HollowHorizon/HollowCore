package ru.hollowhorizon.hc.client.model.dae.loader.model.animator;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;

import javax.annotation.Nonnull;

/**
 * Handles the animation of {@link ru.hollowhorizon.hc.client.model.dae.loader.model.IModel}
 */
@OnlyIn(Dist.CLIENT)
public interface IAnimator
{
    default void startAnimation(@Nonnull final IAnimation animation)
    {
        startAnimation(animation, 0);
    }

    /**
     * Starts a new animation that runs an infinite amount of times with the given priority.
     * Lower priority means more influence on the model.
     *
     * @param animation The animation to start.
     * @param priority The priority. Lower means more influence.
     */
    default void startAnimation(@Nonnull final IAnimation animation, final int priority)
    {
        startAnimation(animation, priority, Double.POSITIVE_INFINITY);
    }

    /**
     * Starts a new animation that runs the given amount of times, with the given priority.
     * Lower priority means more influence on the model.
     *
     * @param animation The animation to start.
     * @param priority The priority.
     * @param count The count.
     */
    void startAnimation(@Nonnull final IAnimation animation, int priority, double count);

    /**
     * Stops a animation from running and removes its information from the animator.
     *
     * @param name The name of the animation.
     */
    void stopAnimation(@Nonnull final ResourceLocation name);

    /**
     * Called to update the animator and the animations that are running.
     * Applies the joint pose of all animations to the models skeleton.
     */
    void onUpdate();

    /**
     * Invoked before the rendering happens.
     * Allows the joint transforms to be updated.
     */
    void onPreRender();
}
