package ru.hollowhorizon.hc.client.model.dae.loader.model.animation;

import java.util.Map;

/**
 * Represents a single moment in an animation.
 */
public interface IKeyFrame
{
    /**
     * The moment after the start of the animation that this KeyFrame triggers.
     *
     * @return the amount of thicks after the start of the animation before this keyframe triggers.
     */
    int getTicksAfterStart();

    /**
     * A map that returns transformations for joints that have to be reached when this keyframe triggers.
     * If the map does not contain a transform for a joint, then this keyframe has no influence on that joint.
     *
     * @return This keyframes joint tranforms.
     */
    Map<String, IJointTransform> getJointTransformMap();
}
