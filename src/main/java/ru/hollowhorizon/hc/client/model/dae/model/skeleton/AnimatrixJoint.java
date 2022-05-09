package ru.hollowhorizon.hc.client.model.dae.model.skeleton;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.IJoint;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Represents a single joint/joint in a Skeleton.
 */
public class AnimatrixJoint implements IJoint {
    private final int index;
    private final String name;
    private final Matrix4f jointSpaceBindTransform;
    private final ImmutableCollection<IJoint> childJoints;
    private Matrix4f animationModelSpaceTransform = new Matrix4f();
    private Matrix4f inverseModelSpaceBindTransform = new Matrix4f();

    /**
     * Creates a new joint for a {@link AnimatrixSkeleton}.
     *
     * @param index                   The index of the joint.
     * @param name                    The name of the joint.
     * @param jointSpaceBindTransform The matrix that defines the position and orientation of the joint compared to its parent.
     * @param childJoints             The joints that this joint is a parent of.
     */
    public AnimatrixJoint(final int index, final String name, final Matrix4f jointSpaceBindTransform, final Collection<IJoint> childJoints) {
        this.index = index;
        this.name = name;
        this.jointSpaceBindTransform = jointSpaceBindTransform;
        this.childJoints = ImmutableList.copyOf(childJoints);
    }

    /**
     * Returns the index of this joint in the skeleton.
     * The index is mostly used by the Shader to access the joint information in the data array.
     *
     * @return The index of the joint.
     */
    @Override
    public int getIndex() {
        return index;
    }

    /**
     * Returns the name of this joint.
     * The name is mostly used during animations to figure out what joints are involved in the keyframes
     * and as such in the entire animation.
     *
     * @return The name of the joint.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Returns the current animation transform of this Joint in the model.
     * The matrix is in ModelSpace so it can be directly transfered to the GPU.
     *
     * @return The animation model space transform matrix of this joint.
     */
    @Override
    public Matrix4f getAnimationModelSpaceTransform() {
        return animationModelSpaceTransform;
    }

    /**
     * Sets the animation modelspace transform matrix for this joint.
     * Identity if the current joint is not moved in the animation.
     *
     * @param animationModelSpaceTransform The current animation based modelspace transform matrix.
     */
    @Override
    public void setAnimationModelSpaceTransform(final Matrix4f animationModelSpaceTransform) {
        this.animationModelSpaceTransform = animationModelSpaceTransform;
    }

    /**
     * Returns the model space inverse transformation of this joint.
     * <p>
     * Model space is the space of the model.
     * The origin of said space if the origin of the model.
     *
     * @return
     */
    @Override
    public Matrix4f getInverseModelSpaceBindTransform() {
        return inverseModelSpaceBindTransform;
    }

    /**
     * Returns the joints children.
     * This joints position is the origin of the Joint space of the joints in the collection.
     *
     * @return The child joints.
     */
    @Override
    public ImmutableCollection<IJoint> getChildJoints() {
        return childJoints;
    }

    /**
     * Updates the {@link AnimatrixJoint#inverseModelSpaceBindTransform} form the given matrix.
     *
     * @param parentModelSpaceBindTransform The parent model space transform.
     */
    @Override
    public void calculateIMSBT(@Nonnull final Matrix4f parentModelSpaceBindTransform) {
        final Matrix4f msbt = Matrix4f.mul(parentModelSpaceBindTransform, jointSpaceBindTransform, null);
        Matrix4f.invert(msbt, inverseModelSpaceBindTransform);

        getChildJoints().forEach(joint -> joint.calculateIMSBT(msbt));
    }

    @Override
    public String toString() {
        return "AnimatrixJoint{" +
                "index=" + index +
                ", name='" + name + '\'' +
                '}';
    }
}


