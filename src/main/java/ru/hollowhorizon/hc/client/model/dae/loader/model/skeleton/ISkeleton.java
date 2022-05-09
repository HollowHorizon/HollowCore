package ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton;

import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

public interface ISkeleton
{
    /**
     * Returns the root Joint for the skeleton.
     * @return The root joint for the skeleton.
     */
    IJoint getRootJoint();

    /**
     * The total amount of joints that make up this skeleton.
     * @return The amount of joints in the skeleton.
     */
    int getJointCount();

    /**
     * Gets an array of the model-space transforms of all the
     * joints (with the current animation pose applied) in the skelton. The
     * joints are ordered in the array based on their joint index. The position
     * of each joint's transform in the array is equal to the joint's index.
     *
     * @return The array of model-space transforms of the joints in the current
     *         animation pose.
     */
    Matrix4f[] getAnimationModelSpaceTransformsFromJoints();
}
