package ru.hollowhorizon.hc.client.model.dae.loader.model;

import io.netty.util.internal.ConcurrentSet;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimatedModelData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.JointData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.MeshData;
import ru.hollowhorizon.hc.client.model.dae.loader.model.collada.IModelLoader;
import ru.hollowhorizon.hc.client.model.dae.loader.model.collada.IModelLoaderManager;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.IJoint;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skeleton.ISkeleton;
import ru.hollowhorizon.hc.client.model.dae.loader.model.skin.ISkin;
import ru.hollowhorizon.hc.client.model.dae.model.AnimatrixModel;
import ru.hollowhorizon.hc.client.model.dae.model.skeleton.AnimatrixJoint;
import ru.hollowhorizon.hc.client.model.dae.model.skeleton.AnimatrixSkeleton;
import ru.hollowhorizon.hc.client.model.dae.model.skin.AnimatrixSkin;
import ru.hollowhorizon.hc.client.render.game.GPUMemoryManager;
import ru.hollowhorizon.hc.client.render.game.VAO;

import java.util.Collection;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ModelLoaderManager implements IModelLoaderManager
{
    private final ConcurrentSet<IModelLoader> loaders = new ConcurrentSet<>();

    @Override
    public IModel loadModel(final ResourceLocation location, final ResourceLocation texture)
    {
        final AnimatedModelData data = loaders.stream().filter(l -> l.canLoadModel(location)).findFirst().orElseThrow(() -> new IllegalArgumentException("Not supported model file: " + location)).loadModel(location);
        final ISkin skin = new AnimatrixSkin(createVAO(data.getMeshData()), texture);
        final ISkeleton skeleton = new AnimatrixSkeleton(createJoints(data.getJointsData().getHeadJoint()));

        return new AnimatrixModel(skeleton, skin);
    }

    /**
     * Constructs the joint-hierarchy skeleton from the data extracted from the
     * collada file.
     *
     * @param data
     *            - the joints data from the collada file for the head joint.
     * @return The created joint, with all its descendants added.
     */
    private static IJoint createJoints(final JointData data) {
        final Collection<IJoint> childJoints = data.getChildren().stream().map(ModelLoaderManager::createJoints).collect(Collectors.toList());
        return new AnimatrixJoint(data.getIndex(), data.getNameId(), data.getBindLocalTransform(), childJoints);
    }

    /**
     * Stores the mesh data in a VAO.
     *
     * @param data
     *            - all the data about the mesh that needs to be stored in the
     *            VAO.
     * @return The VAO containing all the mesh data for the model.
     */
    private static VAO createVAO(final MeshData data) {
        final VAO meshVao = GPUMemoryManager.INSTANCE.createVAO();
        meshVao.bind();
        meshVao.createIndexBuffer(data.getIndices());
        meshVao.createAttribute(0, data.getVertices(), 3);
        meshVao.createAttribute(1, data.getTextureCoords(), 2);
        meshVao.createAttribute(2, data.getNormals(), 3);
        meshVao.createIntAttribute(3, data.getJointIds(), 3);
        meshVao.createAttribute(4, data.getVertexWeights(), 3);
        meshVao.unbind();
        return meshVao;
    }

    @Override
    public void registerLoader(final IModelLoader loader)
    {
        loaders.add(loader);
    }
}
