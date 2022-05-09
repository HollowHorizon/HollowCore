package ru.hollowhorizon.hc.client.model.dae.loader.animation;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.AnimationLoadingException;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.IAnimationLoader;
import ru.hollowhorizon.hc.client.model.dae.loader.animation.collada.IAnimationLoaderManager;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimationData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.JointTransformData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.KeyFrameData;
import ru.hollowhorizon.hc.client.model.dae.loader.model.IModel;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IAnimation;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IJointTransform;
import ru.hollowhorizon.hc.client.model.dae.loader.model.animation.IKeyFrame;
import ru.hollowhorizon.hc.client.model.dae.model.animation.AnimatrixAnimation;
import ru.hollowhorizon.hc.client.model.dae.model.animation.AnimatrixJointTransform;
import ru.hollowhorizon.hc.client.model.dae.model.animation.AnimatrixKeyFrame;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AnimationLoaderManager implements IAnimationLoaderManager
{
    private final ConcurrentSet<IAnimationLoader> loaders = new ConcurrentSet<>();

    @Override
    public IAnimation loadAnimation(final IModel model, final ResourceLocation location) throws AnimationLoadingException
    {
       final AnimationData data = loaders.stream().filter(loader -> loader.canLoadAnimation(location)).findFirst().orElseThrow(() -> new IllegalArgumentException("Not supported animation file: " + location)).loadAnimation(location);
       final int lengthInTicks = (int) (data.getLengthSeconds() * 20);
       final IKeyFrame[] keyFrames = Arrays.stream(data.getKeyFrames()).map(AnimationLoaderManager::createKeyFrame).toArray(IKeyFrame[]::new);

       return new AnimatrixAnimation(location, model, lengthInTicks, keyFrames);
    }

    /**
     * Creates a keyframe from the data extracted from the collada file.
     *
     * @param data the data about the keyframe that was extracted from the
     *            collada file.
     * @return The keyframe.
     */
    private static IKeyFrame createKeyFrame(final KeyFrameData data) {
        final Map<String, IJointTransform> map = new HashMap<>();
        for (final JointTransformData jointData : data.getJointTransforms()) {
            final IJointTransform jointTransform = createTransform(jointData);
            map.put(jointData.getJointNameId(), jointTransform);
        }
        return new AnimatrixKeyFrame((int) (data.getTime() * 20), map);
    }

    /**
     * Creates a joint transform from the data extracted from the collada file.
     *
     * @param data
     *            - the data from the collada file.
     * @return The joint transform.
     */
    private static IJointTransform createTransform(final JointTransformData data) {
        final Matrix4f mat = data.getJointLocalTransform();
        final Vector3f translation = new Vector3f(mat.m30, mat.m31, mat.m32);
        final Quaternion rotation = fromMatrix(mat);
        return new AnimatrixJointTransform(data.getJointNameId(), translation, rotation);
    }

    public static Quaternion fromMatrix(final Matrix4f matrix) {
        final float w;
        final float x;
        final float y;
        final float z;
        final float diagonal = matrix.m00 + matrix.m11 + matrix.m22;
        if (diagonal > 0) {
            final float w4 = (float) (Math.sqrt(diagonal + 1f) * 2f);
            w = w4 / 4f;
            x = (matrix.m21 - matrix.m12) / w4;
            y = (matrix.m02 - matrix.m20) / w4;
            z = (matrix.m10 - matrix.m01) / w4;
        } else if ((matrix.m00 > matrix.m11) && (matrix.m00 > matrix.m22)) {
            final float x4 = (float) (Math.sqrt(1f + matrix.m00 - matrix.m11 - matrix.m22) * 2f);
            w = (matrix.m21 - matrix.m12) / x4;
            x = x4 / 4f;
            y = (matrix.m01 + matrix.m10) / x4;
            z = (matrix.m02 + matrix.m20) / x4;
        } else if (matrix.m11 > matrix.m22) {
            final float y4 = (float) (Math.sqrt(1f + matrix.m11 - matrix.m00 - matrix.m22) * 2f);
            w = (matrix.m02 - matrix.m20) / y4;
            x = (matrix.m01 + matrix.m10) / y4;
            y = y4 / 4f;
            z = (matrix.m12 + matrix.m21) / y4;
        } else {
            final float z4 = (float) (Math.sqrt(1f + matrix.m22 - matrix.m00 - matrix.m11) * 2f);
            w = (matrix.m10 - matrix.m01) / z4;
            x = (matrix.m02 + matrix.m20) / z4;
            y = (matrix.m12 + matrix.m21) / z4;
            z = z4 / 4f;
        }
        return new Quaternion(x, y, z, w);
    }

    @Override
    public void registerLoader(final IAnimationLoader loader)
    {
        loaders.add(loader);
    }
}
