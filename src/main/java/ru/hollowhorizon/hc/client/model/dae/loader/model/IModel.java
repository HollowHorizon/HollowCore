package ru.hollowhorizon.hc.client.model.dae.loader.model;

import ru.hollowhorizon.hc.client.model.dae.loader.model.animator.IAnimator;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.ISkeleton;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skin.ISkin;

/**
 * Represents a single model in Animatrix.
 */
public interface IModel
{
    /**
     * The skeleton of the model.
     *
     * @return The skeleton.
     */
    ISkeleton getSkeleton();

    /**
     * The skin of the model.
     *
     * @return The skin.
     */
    ISkin getSkin();

    /**
     * The animator for the model.
     *
     * @return The animator.
     */
    IAnimator getAnimator();
}
