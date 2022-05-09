package ru.hollowhorizon.hc.client.model.dae.loader.model.animation;

import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import java.util.Map;
import java.util.function.Consumer;

public interface IAnimation {

    /**
     * Returns the name of the animation.
     *
     * @return The name of the animation.
     */
    ResourceLocation getName();

    /**
     * Returns to total length of the animation in ticks.
     *
     * @return The total length of the animation in ticks.
     */
    int getTotalLengthInTicks();

    /**
     * The keyframes of this animation.
     *
     * @return The keyframes of the animation.
     */
    IKeyFrame[] getKeyFrames();

    /**
     * This method should be called each frame to update the animation currently
     * being played. This increases the animation time (and loops it back to
     * zero if necessary), finds the pose that the entity should be in at that
     * time of the animation, and then applies that pose to all the model's
     * joints by setting the joint transforms.
     *
     * @param onAnimationCompleted Callback called when animation completes.
     */
    void update(Consumer<IAnimation> onAnimationCompleted);

    /**
     * This method returns the current animation pose of the entity. It returns
     * the desired local-space transforms for all the joints in a map, indexed
     * by the name of the joint that they correspond to.
     * <p>
     * The pose is calculated based on the previous and next keyframes in the
     * current animation. Each keyframe provides the desired pose at a certain
     * time in the animation, so the animated pose for the current time can be
     * calculated by interpolating between the previous and next keyframe.
     * <p>
     * This method first finds the preious and next keyframe, calculates how far
     * between the two the current animation is, and then calculated the pose
     * for the current animation time by interpolating between the transforms at
     * those keyframes.
     *
     * @return The current pose as a map of the desired local-space transforms
     * for all the joints. The transforms are indexed by the name ID of
     * the joint that they should be applied to.
     */
    Map<String, Matrix4f> calculateCurrentAnimationPose();
}
