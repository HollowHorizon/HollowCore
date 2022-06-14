package ru.hollowhorizon.hc.client.model.dae.model.animator;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animator.IAnimator;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animator.IAnimatorAnimationInformation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.IJoint;
import ru.hollowhorizon.hc.client.model.dae.model.AnimatrixModel;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handles the animation of {@link AnimatrixModel}
 */
@OnlyIn(Dist.CLIENT)
public class AnimatrixAnimator implements IAnimator {

    private final IModel model;
    private final List<IAnimatorAnimationInformation> runningAnimations = Lists.newLinkedList();

    public AnimatrixAnimator(final IModel model) {
        this.model = model;
    }

    /**
     * Starts a new animation that runs the given amount of times, with the given priority.
     * Lower priority means more influence on the model.
     *
     * @param animation The animation to start.
     * @param priority  The priority.
     * @param count     The count.
     */
    @Override
    public void startAnimation(@Nonnull final IAnimation animation, int priority, final double count) {
        if (runningAnimations.stream().anyMatch(information -> information.getAnimation().getName().equals(animation.getName())))
            return;

        if (priority < 0)
            priority = 0;

        if (priority > this.runningAnimations.size())
            priority = this.runningAnimations.size();

        this.runningAnimations.add(priority, new AnimatrixAnimatorAnimationInformation(animation, Math.ceil(count)));
    }

    /**
     * Stops a animation from running and removes its information from the animator.
     *
     * @param name The name of the animation.
     */
    @Override
    public void stopAnimation(@Nonnull final ResourceLocation name) {
        final Optional<IAnimatorAnimationInformation> animationInformationOptional =
                runningAnimations.stream().filter(information -> information.getAnimation().getName().equals(name)).findFirst();

        animationInformationOptional.ifPresent(runningAnimations::remove);
    }

    /**
     * Called to update the animator and the animations that are running.
     * Applies the joint pose of all animations to the models skeleton.
     */
    @Override
    public void onUpdate() {
        final ArrayList<IAnimatorAnimationInformation> finishedAnimations = Lists.newArrayList();
        runningAnimations.forEach(iAnimatorAnimationInformation -> iAnimatorAnimationInformation.update(finishedAnimations::add));
        runningAnimations.removeAll(finishedAnimations);
    }

    /**
     * Invoked before the rendering happens. Allows the joint transforms to be updated.
     */
    @Override
    public void onPreRender() {
        applyAllPosesToJoints();
    }

    /**
     * This method combines all the animations poses for the joints together.
     */
    private void applyAllPosesToJoints() {
        final Map<String, Matrix4f> finalJointPoses = Maps.newHashMap();
        for (final IAnimatorAnimationInformation information : runningAnimations) {
            final Map<String, Matrix4f> animationJointPose = information.getAnimation().calculateCurrentAnimationPose();
            animationJointPose.forEach((jointName, poseMatrix) -> finalJointPoses.merge(jointName, poseMatrix, (existingPose, additionalMovement) -> Matrix4f.mul(additionalMovement, existingPose, null)));
        }

        applyPoseToJoints(finalJointPoses, model.getSkeleton().getRootJoint(), new Matrix4f());

    }

    /**
     * This is the method where the animator calculates and sets those all-
     * important "joint transforms" that I talked about so much in the tutorial.
     * <p>
     * This method applies the current pose to a given joint, and all of its
     * descendants. It does this by getting the desired local-transform for the
     * current joint, before applying it to the joint. Before applying the
     * transformations it needs to be converted from local-space to model-space
     * (so that they are relative to the model's origin, rather than relative to
     * the parent joint). This can be done by multiplying the local-transform of
     * the joint with the model-space transform of the parent joint.
     * <p>
     * The same thing is then done to all the child joints.
     * <p>
     * Finally the inverse of the joint's bind transform is multiplied with the
     * model-space transform of the joint. This basically "subtracts" the
     * joint's original bind (no animation applied) transform from the desired
     * pose transform. The result of this is then the transform required to move
     * the joint from its original model-space transform to it's desired
     * model-space posed transform. This is the transform that needs to be
     * loaded up to the vertex shader and used to transform the vertices into
     * the current pose.
     *
     * @param currentPose     a map of the local-space transforms for all the joints for
     *                        the desired pose. The map is indexed by the name of the joint
     *                        which the transform corresponds to.
     * @param joint           the current joint which the pose should be applied to.
     * @param parentTransform the desired model-space transform of the parent joint for
     *                        the pose.
     */
    private void applyPoseToJoints(final Map<String, Matrix4f> currentPose, final IJoint joint, final Matrix4f parentTransform) {
        final Matrix4f currentLocalTransform = currentPose.containsKey(joint.getName()) ? currentPose.get(joint.getName()) : new Matrix4f();
        final Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
        for (final IJoint childJoint : joint.getChildJoints()) {
            applyPoseToJoints(currentPose, childJoint, currentTransform);
        }

        final Matrix4f jointInverse;
        if (runningAnimations.isEmpty()) {
            jointInverse = new Matrix4f();
        } else {
            jointInverse = joint.getInverseModelSpaceBindTransform();
        }

        Matrix4f.mul(currentTransform, jointInverse, currentTransform);
        joint.setAnimationModelSpaceTransform(currentTransform);
    }

    @Override
    public String toString() {
        return "AnimatrixAnimator{" +
                ", runningAnimations=" + runningAnimations +
                '}';
    }
}
