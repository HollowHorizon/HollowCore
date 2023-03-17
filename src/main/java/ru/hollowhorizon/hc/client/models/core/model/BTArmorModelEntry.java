package ru.hollowhorizon.hc.client.models.core.model;

import net.minecraft.client.Minecraft;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.BoneTownConstants;
import ru.hollowhorizon.hc.client.models.core.BoneTownRegistry;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFArmorModel;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModelLoader;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class BTArmorModelEntry implements IForgeRegistryEntry<BTArmorModelEntry> {
    private ResourceLocation name;
    public ResourceLocation modelName;
    public ResourceLocation modelFile;
    private BoneTownConstants.MeshTypes meshType;
    private final List<String> headMeshes;
    private final List<String> bodyMeshes;
    private final List<String> legMeshes;
    private final List<String> feetMeshes;
    private final IArmorMaterial material;

    public BTArmorModelEntry(ResourceLocation modelName,
                             ResourceLocation modelFile,
                             List<String> headMeshes, List<String> bodyMeshes,
                             List<String> legMeshes, List<String> feetMeshes){
        this(modelName, modelFile, headMeshes, bodyMeshes, legMeshes, feetMeshes, null);
    }

    public BTArmorModelEntry(ResourceLocation modelName,
                             ResourceLocation modelFile,
                             List<String> headMeshes, List<String> bodyMeshes,
                             List<String> legMeshes, List<String> feetMeshes,
                             IArmorMaterial material){
        this.headMeshes = headMeshes;
        this.bodyMeshes = bodyMeshes;
        this.legMeshes = legMeshes;
        this.feetMeshes = feetMeshes;
        this.modelName = modelName;
        this.modelFile = modelFile;
        this.material = material;
        this.meshType = BoneTownConstants.MeshTypes.BONEMF;
    }

    @Override
    public BTArmorModelEntry setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    public void load(){
        String meshExt = BoneTownConstants.stringFromMeshType(meshType);
        ResourceLocation name = getRegistryName();
        ResourceLocation animLocation = new ResourceLocation(modelFile.getNamespace(),
                BoneTownConstants.BONETOWN_MODELS_DIR +
                        "/" + modelFile.getPath() + "." + meshExt);
        BTModel model = BoneTownRegistry.MODEL_REGISTRY.getValue(this.modelName);
        if (model instanceof BTAnimatedModel){
            try {
                InputStream stream = Minecraft.getInstance().getResourceManager()
                        .getResource(animLocation)
                        .getInputStream();
                byte[] _data = IOUtils.toByteArray(stream);
                ByteBuffer data = MemoryUtil.memCalloc(_data.length + 1);
                data.put(_data);
                data.put((byte) 0);
                data.flip();
                stream.close();
                try {
                    BoneMFArmorModel armorModel = BoneMFModelLoader.loadArmor(data, name, headMeshes,
                            bodyMeshes, legMeshes, feetMeshes);
                    if (material == null){
                        ((BTAnimatedModel) model).addDefaultArmor(armorModel);
                    } else {
                        ((BTAnimatedModel) model).addArmorOverride(material, armorModel);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MemoryUtil.memFree(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            HollowCore.LOGGER.error("Trying to load {} armor models for animated model: {}, " +
                    "but model wasn't found or it is not animated", getRegistryName(), modelName);
        }


    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<BTArmorModelEntry> getRegistryType() {
        return BTArmorModelEntry.class;
    }
}


