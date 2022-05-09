package ru.hollowhorizon.hc.client.model.dae.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IJointTransform;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IKeyFrame;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Represents a simple animation in Animatix.
 */
@OnlyIn(Dist.CLIENT)
public class AnimatrixAnimation implements IAnimation
{

    private final ResourceLocation name;

    private final IModel model;

    private final int             totalLengthInTicks;
    private       int             animationTime;

    private final IKeyFrame[] keyFrames;

    public AnimatrixAnimation(final ResourceLocation name, final IModel model, final int totalLengthInTicks, final IKeyFrame[] keyFrames) {
        this.name = name;
        this.model = model;
        this.totalLengthInTicks = totalLengthInTicks;
        this.keyFrames = keyFrames;
        this.animationTime = 0;
    }

    @Override
    public ResourceLocation getName()
    {
        return name;
    }

    /**
     * Returns to total length of the animation in ticks.
     *
     * @return The total length of the animation in ticks.
     */
    @Override
    public int getTotalLengthInTicks()
    {
        return totalLengthInTicks;
    }

    /**
     * The keyframes of this animation.
     *
     * @return The keyframes of the animation.
     */
    @Override
    public IKeyFrame[] getKeyFrames()
    {
        return keyFrames;
    }

    /**
     * This method should be called each frame to update the animation currently
     * being played. This increases the animation time (and loops it back to
     * zero if necessary), finds the pose that the entity should be in at that
     * time of the animation, and then applies that pose to all the model's
     * joints by setting the joint transforms.
     *
     * @param onAnimationCompleted Callback called when animation completes.
     */
    @Override
    public void update(final Consumer<IAnimation> onAnimationCompleted) {
        increaseAnimationTime(onAnimationCompleted);
    }

    /**
     * Increases the current animation time which allows the animation to
     * progress. If the current animation has reached the end then the timer is
     * reset, causing the animation to loop.
     */
    private void increaseAnimationTime(final Consumer<IAnimation> onAnimationCompleted) {
        animationTime += 1;
        if (animationTime >= getTotalLengthInTicks()) {
            onAnimationCompleted.accept(this);
            this.animationTime %= getTotalLengthInTicks();
        }
    }

    /**
     * This method returns the current animation pose of the entity. It returns
     * the desired local-space transforms for all the joints in a map, indexed
     * by the name of the joint that they correspond to.
     *
     * The pose is calculated based on the previous and next keyframes in the
     * current animation. Each keyframe provides the desired pose at a certain
     * time in the animation, so the animated pose for the current time can be
     * calculated by interpolating between the previous and next keyframe.
     *
     * This method first finds the preious and next keyframe, calculates how far
     * between the two the current animation is, and then calculated the pose
     * for the current animation time by interpolating between the transforms at
     * those keyframes.
     *
     * @return The current pose as a map of the desired local-space transforms
     *         for all the joints. The transforms are indexed by the name ID of
     *         the joint that they should be applied to.
     */
    @Override
    public Map<String, Matrix4f> calculateCurrentAnimationPose() {
        final IKeyFrame[] frames = getPreviousAndNextFrames();
        final float progression = calculateProgression(frames[0], frames[1]);
        return interpolatePoses(frames[0], frames[1], progression);
    }

    /**
     * Finds the previous keyframe in the animation and the next keyframe in the
     * animation, and returns them in an array of length 2. If there is no
     * previous frame (perhaps current animation time is 0.5 and the first
     * keyframe is at time 1.5) then the first keyframe is used as both the
     * previous and next keyframe. The last keyframe is used for both next and
     * previous if there is no next keyframe.
     *
     * @return The previous and next keyframes, in an array which therefore will
     *         always have a length of 2.
     */
    private IKeyFrame[] getPreviousAndNextFrames() {
        IKeyFrame previousFrame = getKeyFrames()[0];
        IKeyFrame nextFrame = getKeyFrames()[0];
        for (int i = 1; i < getKeyFrames().length; i++) {
            nextFrame = getKeyFrames()[i];
            if (nextFrame.getTicksAfterStart() > animationTime) {
                break;
            }
            previousFrame = getKeyFrames()[i];
        }
        return new IKeyFrame[] { previousFrame, nextFrame };
    }

    /**
     * Calculates how far between the previous and next keyframe the current
     * animation time is, and returns it as a value between 0 and 1.
     *
     * @param previousFrame the previous keyframe in the animation.
     * @param nextFrame the next keyframe in the animation.
     * @return A number between 0 and 1 indicating how far between the two
     *         keyframes the current animation time is.
     */
    private float calculateProgression(final IKeyFrame previousFrame, final IKeyFrame nextFrame) {
        final float totalTime = nextFrame.getTicksAfterStart() - previousFrame.getTicksAfterStart();
        final float currentTime = (animationTime + Minecraft.getInstance().getDeltaFrameTime()) - previousFrame.getTicksAfterStart();
        return currentTime / totalTime;
    }

    /**
     * Calculates all the local-space joint transforms for the desired current
     * pose by interpolating between the transforms at the previous and next
     * keyframes.
     *
     * @param previousFrame previous keyframe in the animation.
     * @param nextFrame the next keyframe in the animation.
     * @param progression a number between 0 and 1 indicating how far between the
     *            previous and next keyframes the current animation time is.
     * @return The local-space transforms for all the joints for the desired
     *         current pose. They are returned in a map, indexed by the name of
     *         the joint to which they should be applied.
     */
    private Map<String, Matrix4f> interpolatePoses(final IKeyFrame previousFrame, final IKeyFrame nextFrame, final float progression) {
        final Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
        for (final String jointName : previousFrame.getJointTransformMap().keySet()) {
            final IJointTransform previousTransform = previousFrame.getJointTransformMap().get(jointName);
            final IJointTransform nextTransform = nextFrame.getJointTransformMap().get(jointName);
            final IJointTransform currentTransform = interpolateJoint(previousTransform, nextTransform, progression, AnimatrixJointTransform::new);
            currentPose.put(jointName, currentTransform.getJointSpaceTransformMatrix());
        }
        return currentPose;
    }

    public static IJointTransform interpolateJoint(final IJointTransform frameA, final IJointTransform frameB, final float progression, final IJointTransformConstructor constructor) {
        if (!frameA.getJointName().equals(frameB.getJointName()))
            throw new IllegalArgumentException("A and B have different Joints");

        final Vector3f pos = interpolateVec(frameA.getPosition(), frameB.getPosition(), progression);
        final Quaternion rot = interpolateQuat(frameA.getRotation(), frameB.getRotation(), progression);
        return constructor.apply(frameA.getJointName(), pos, rot);
    }

    public static Quaternion interpolateQuat(final Quaternion a, final Quaternion b, final float blend) {
        final Quaternion result = new Quaternion(0, 0, 0, 1);
        final float dot = a.r() * b.r() + a.i() * b.i() + a.j() * b.j() + a.k() * b.k();
        final float blendI = 1f - blend;
        if (dot < 0) {
            result.set(
                    blendI * a.i() + blend * -b.i(),
                    blendI * a.j() + blend * -b.j(),
                    blendI * a.k() + blend * -b.k(),
                    blendI * a.r() + blend * -b.r()
            );
        } else {
            result.set(
                    blendI * a.i() + blend * b.i(),
                    blendI * a.j() + blend * b.j(),
                    blendI * a.k() + blend * b.k(),
                    blendI * a.r() + blend * b.r()
            );
        }
        result.normalize();
        return result;
    }

    public static Vector3f interpolateVec(final Vector3f start, final Vector3f end, final float progression) {
        final float x = start.x() + (end.x() - start.x()) * progression;
        final float y = start.y() + (end.y() - start.y()) * progression;
        final float z = start.z() + (end.z() - start.z()) * progression;
        return new Vector3f(x, y, z);
    }

    @FunctionalInterface
    public interface IJointTransformConstructor
    {
        /**
         * Creates a new {@link IJointTransform} from the given name, position and rotation.
         *
         * @param jointName The name of the joint.
         * @param position The position of the joint.
         * @param rotation The rotation of the joint.
         *
         * @return The new {@link IJointTransform} for the given name, position and rotation.
         */
        IJointTransform apply(final String jointName, final Vector3f position, final Quaternion rotation);
    }

    @Override
    public String toString()
    {
        return "AnimatrixAnimation{" +
                 "totalLengthInTicks=" + totalLengthInTicks +
                 ", animationTime=" + animationTime +
                 ", keyFrames=" + Arrays.toString(keyFrames) +
                 '}';
    }
}
