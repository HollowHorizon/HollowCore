package ru.hollowhorizon.hc.client.models.core.model;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.BoneTownConstants;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModel;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModelLoader;
import ru.hollowhorizon.hc.common.registry.BTMaterials;


import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BTModel implements IForgeRegistryEntry<BTModel> {
    private ResourceLocation location;
    private ResourceLocation programName;
    public final BoneTownConstants.MeshTypes meshType;
    protected BoneMFModel model;
    private BakedMesh[] meshes;
    private BakedMesh combinedMesh;

    public BTModel(BoneTownConstants.MeshTypes meshType){
        this(BTMaterials.DEFAULT_STATIC_LOC, meshType);
    }


    public BTModel(ResourceLocation programName,
                   BoneTownConstants.MeshTypes meshType){
        this.meshType = meshType;
        this.programName = programName;
    }

    public BakedMesh getCombinedMesh() {
        return combinedMesh;
    }

    public BakedMesh[] getMeshes(){
        return meshes;
    }

    public BoneMFModel getModel() {
        return model;
    }

    public void load(){
        HollowCore.LOGGER.info("Loading model: " + getRegistryName());
        String meshExt = BoneTownConstants.stringFromMeshType(meshType);
        ResourceLocation name = getRegistryName();
        ResourceLocation meshLocation = new ResourceLocation(name.getNamespace(),
                BoneTownConstants.BONETOWN_MODELS_DIR +
                        "/" + name.getPath() + "." + meshExt);
        try {
            InputStream stream = Minecraft.getInstance().getResourceManager()
                    .getResource(meshLocation)
                    .getInputStream();
            byte[] _data = IOUtils.toByteArray(stream);
            ByteBuffer data = MemoryUtil.memCalloc(_data.length + 1);
            data.put(_data);
            data.put((byte) 0);
            data.flip();
            stream.close();
            try {
                this.model = BoneMFModelLoader.load(data, name);
                this.meshes = model.bakeMeshes().toArray(new BakedMesh[0]);
                this.combinedMesh = model.bakeCombinedMesh();
            } catch (Exception e) {
                e.printStackTrace();
            }
            MemoryUtil.memFree(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BTModel setRegistryName(ResourceLocation name) {
        location = name;
        return this;
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return location;
    }

    @Override
    public Class<BTModel> getRegistryType() {
        return BTModel.class;
    }

    public ResourceLocation getProgramName(){ return programName; }
}
