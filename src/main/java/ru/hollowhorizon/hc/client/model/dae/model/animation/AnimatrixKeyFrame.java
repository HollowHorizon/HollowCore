package ru.hollowhorizon.hc.client.model.dae.model.animation;

import com.google.common.collect.ImmutableMap;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IJointTransform;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IKeyFrame;

import java.util.Map;

/**
 * Represents a single keyframe in an {@link AnimatrixAnimation}.
 */
public class AnimatrixKeyFrame implements IKeyFrame
{
    private final int                                  ticksAfterStart;
    private final Map<String, IJointTransform> jointTransformMap;

    public AnimatrixKeyFrame(final int ticksAfterStart, final Map<String, IJointTransform> jointTransformMap) {
        this.ticksAfterStart = ticksAfterStart;
        this.jointTransformMap = jointTransformMap;
    }

    /**
     * The moment after the start of the animation that this KeyFrame triggers.
     *
     * @return the amount of thicks after the start of the animation before this keyframe triggers.
     */
    @Override
    public int getTicksAfterStart()
    {
        return ticksAfterStart;
    }

    /**
     * A map that returns transformations for joints that have to be reached when this keyframe triggers.
     * If the map does not contain a transform for a joint, then this keyframe has no influence on that joint.
     *
     * @return This keyframes joint tranforms.
     */
    @Override
    public Map<String, IJointTransform> getJointTransformMap()
    {
        return ImmutableMap.copyOf(jointTransformMap);
    }

    @Override
    public String toString()
    {
        return "AnimatrixKeyFrame{" +
                 "ticksAfterStart=" + ticksAfterStart +
                 '}';
    }
}
