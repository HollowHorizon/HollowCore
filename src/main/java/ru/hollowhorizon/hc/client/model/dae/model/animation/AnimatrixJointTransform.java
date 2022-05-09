package ru.hollowhorizon.hc.client.model.dae.model.animation;

import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IJointTransform;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IKeyFrame;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.IJoint;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

/**
 * Represents a single transform of a single {@link IJoint} in a {@link IKeyFrame}
 */
@OnlyIn(Dist.CLIENT)
public class AnimatrixJointTransform implements IJointTransform
{

    private final String jointName;
    private final Vector3f position;
    private final Quaternion rotation;

    public AnimatrixJointTransform(final String jointName, final Vector3f position, final Quaternion rotation) {
        this.jointName = jointName;
        this.position = position;
        this.rotation = rotation;
    }

    /**
     * The name of the {@link IJoint} that is being transformed by this.
     *
     * @return The name of the {@link IJoint}.
     */
    @Override
    public String getJointName()
    {
        return jointName;
    }

    /**
     * The position that the {@link IJoint} needs to be in.
     *
     * @return The position of the {@link IJoint}.
     */
    @Override
    public Vector3f getPosition()
    {
        return position;
    }

    /**
     * The rotation that the {@link IJoint} needs to have.
     *
     * @return The rotation of the {@link IJoint}.
     */
    @Override
    public Quaternion getRotation()
    {
        return rotation;
    }

    /**
     * Returns the transforming matrix in joint space for this.
     *
     * @return The transforming matrix.
     */
    @Override
    public Matrix4f getJointSpaceTransformMatrix()
    {
        final Matrix4f matrix4f = new Matrix4f();
        matrix4f.translate(position);
        Matrix4f.mul(matrix4f, toRotationMatrix(rotation), matrix4f);
        return matrix4f;
    }

    public static Matrix4f toRotationMatrix(final Quaternion quaternion) {
        final Matrix4f matrix = new Matrix4f();
        final float xy = quaternion.i() * quaternion.j();
        final float xz = quaternion.i() * quaternion.k();
        final float xw = quaternion.i() * quaternion.r();
        final float yz = quaternion.j() * quaternion.k();
        final float yw = quaternion.j() * quaternion.r();
        final float zw = quaternion.k() * quaternion.r();
        final float xSquared = quaternion.i() * quaternion.i();
        final float ySquared = quaternion.j() * quaternion.j();
        final float zSquared = quaternion.k() * quaternion.k();
        matrix.m00 = 1 - 2 * (ySquared + zSquared);
        matrix.m01 = 2 * (xy - zw);
        matrix.m02 = 2 * (xz + yw);
        matrix.m03 = 0;
        matrix.m10 = 2 * (xy + zw);
        matrix.m11 = 1 - 2 * (xSquared + zSquared);
        matrix.m12 = 2 * (yz - xw);
        matrix.m13 = 0;
        matrix.m20 = 2 * (xz - yw);
        matrix.m21 = 2 * (yz + xw);
        matrix.m22 = 1 - 2 * (xSquared + ySquared);
        matrix.m23 = 0;
        matrix.m30 = 0;
        matrix.m31 = 0;
        matrix.m32 = 0;
        matrix.m33 = 1;
        return matrix;
    }

    @Override
    public String toString()
    {
        return "AnimatrixJointTransform{" +
                 "jointName='" + jointName + '\'' +
                 ", position=" + position +
                 ", rotation=" + rotation +
                 '}';
    }
}
