package ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton;

import com.google.common.collect.ImmutableCollection;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import javax.annotation.Nonnull;

public interface IJoint
{
    /**
     * Returns the index of this joint in the skeleton.
     * The index is mostly used by the Shader to access the joint information in the data array.
     *
     * @return The index of the joint.
     */
    int getIndex();

    /**
     * Returns the name of this joint.
     * The name is mostly used during animations to figure out what joints are involved in the keyframes
     * and as such in the entire animation.
     *
     * @return The name of the joint.
     */
    String getName();

    /**
     * Returns the current animation transform of this Joint in the model.
     * The matrix is in ModelSpace so it can be directly transfered to the GPU.
     *
     * @return The animation model space transform matrix of this joint.
     */
    Matrix4f getAnimationModelSpaceTransform();

    /**
     * Sets the animation modelspace transform matrix for this joint.
     * Identity if the current joint is not moved in the animation.
     *
     * @param animationModelSpaceTransform The current animation based modelspace transform matrix.
     */
    void setAnimationModelSpaceTransform(Matrix4f animationModelSpaceTransform);

    /**
     * Returns the joint space transformation of this joint.
     *
     * Joint space is the space of the joint.
     * The origin of said space if the parent joint.
     *
     * @return
     */
    Matrix4f getInverseModelSpaceBindTransform();

    /**
     * Returns the joints children.
     * This joints position is the origin of the Joint space of the joints in the collection.
     *
     * @return The child joints.
     */
    ImmutableCollection<IJoint> getChildJoints();

    /**
     * Returns the max joint index..
     *
     * @return The max joint index.
     */
    default int getMaxJointIndex()
    {
        return Math.max(getChildJoints().stream().mapToInt(IJoint::getMaxJointIndex).max().orElse(0), getIndex());
    }

    /**
     * Updates the inverseModelSpaceBindTransform form the given matrix.
     *
     * @param parentModelSpaceBindTransform The parent modelspace bind transform matrix.
     */
    void calculateIMSBT(@Nonnull Matrix4f parentModelSpaceBindTransform);
}
