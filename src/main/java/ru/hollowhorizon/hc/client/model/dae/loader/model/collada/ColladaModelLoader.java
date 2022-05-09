package ru.hollowhorizon.hc.client.model.dae.loader.model.collada;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimatedModelData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.MeshData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.SkeletonData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.SkinningData;
import ru.hollowhorizon.hc.client.utils.tools.XmlNode;
import ru.hollowhorizon.hc.client.utils.tools.XmlParser;

import javax.annotation.Nonnull;

import static ru.hollowhorizon.hc.HollowCore.MODID;

@OnlyIn(Dist.CLIENT)
public class ColladaModelLoader implements IModelLoader
{

    @Override
    public boolean canLoadModel(@Nonnull final ResourceLocation modelLocation)
    {
        return modelLocation.getPath().endsWith(".dae");
    }

    public static void main(String[] args) {
        new ColladaModelLoader().loadModel( new ResourceLocation(MODID, "models/untitled.dae"));
    }

    @Override
    public AnimatedModelData loadModel(final ResourceLocation colladaFile) throws ModelLoadingException {
        try {
            final XmlNode node = XmlParser.loadXmlFile(colladaFile);
            final ColladaSkinLoader skinLoader = new ColladaSkinLoader(node.getChild("library_controllers"), 5);
            final SkinningData skinningData = skinLoader.extractSkinData();


            final ColladaSkeletonLoader jointsLoader = new ColladaSkeletonLoader(node.getChild("library_visual_scenes"), skinningData.getJointOrder());
            final SkeletonData jointsData = jointsLoader.extractBoneData();

            final ColladaGeometryLoader g = new ColladaGeometryLoader(node.getChild("library_geometries"), skinningData.getVerticesSkinData());
            System.out.println("loading models 4");
            final MeshData meshData = g.extractModelData();

            System.out.println("loading models done");
            return new AnimatedModelData(meshData, jointsData);
        }
        catch (final Exception e)
        {
            throw new ModelLoadingException(this.getClass(), colladaFile, e);
        }
	}


}
