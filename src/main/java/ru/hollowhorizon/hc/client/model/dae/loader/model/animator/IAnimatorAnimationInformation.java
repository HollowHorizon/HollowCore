package ru.hollowhorizon.hc.client.model.dae.loader.model.animator;

import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;

import java.util.function.Consumer;

public interface IAnimatorAnimationInformation
{
    /**
     * Call this once per tick to update the animation.
     *
     * @param onAnimationRepetitionEnded Callback called when the animation has ended.
     */
    void update(Consumer<IAnimatorAnimationInformation> onAnimationRepetitionEnded);

    /**
     * Returns the remaining count that the animations still needs to be repeated.
     * @return The
     */
    Double getRemainingCount();

    /**
     * The animation.
     * @return The animation.
     */
    IAnimation getAnimation();
}
