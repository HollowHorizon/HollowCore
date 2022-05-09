package ru.hollowhorizon.hc.client.model.dae.loader.model.animation;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.IJoint;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

/**
 * Represents a single transform of a single joint in a keyframe.
 */
public interface IJointTransform
{
    /**
     * The name of the {@link IJoint} that is being transformed by this.
     *
     * @return The name of the {@link IJoint}.
     */
    String getJointName();

    /**
     * The position that the {@link IJoint} needs to be in.
     *
     * @return The position of the {@link IJoint}.
     */
    Vector3f getPosition();

    /**
     * The rotation that the {@link IJoint} needs to have.
     *
     * @return The rotation of the {@link IJoint}.
     */
    Quaternion getRotation();

    /**
     * Returns the transforming matrix in joint space for this.
     *
     * @return The transforming matrix.
     */
    Matrix4f getJointSpaceTransformMatrix();
}
