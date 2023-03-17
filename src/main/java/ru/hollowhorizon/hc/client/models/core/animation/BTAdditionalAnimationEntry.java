package ru.hollowhorizon.hc.client.models.core.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.commons.io.IOUtils;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.BoneTownConstants;
import ru.hollowhorizon.hc.client.models.core.BoneTownRegistry;
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFModelLoader;
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel;
import ru.hollowhorizon.hc.client.models.core.model.BTModel;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BTAdditionalAnimationEntry implements IForgeRegistryEntry<BTAdditionalAnimationEntry> {

    private ResourceLocation name;
    public ResourceLocation modelName;
    public ResourceLocation animationFile;
    private BoneTownConstants.MeshTypes meshType;

    public BTAdditionalAnimationEntry(ResourceLocation modelName,
                                      ResourceLocation animationFile){
        this.modelName = modelName;
        this.animationFile = animationFile;
        this.meshType = BoneTownConstants.MeshTypes.BONEMF;
    }

    @Override
    public BTAdditionalAnimationEntry setRegistryName(ResourceLocation name) {
        this.name = name;
        return this;
    }

    public void load(){
        String meshExt = BoneTownConstants.stringFromMeshType(meshType);
        ResourceLocation name = getRegistryName();
        ResourceLocation animLocation = new ResourceLocation(animationFile.getNamespace(),
                BoneTownConstants.BONETOWN_ANIMATIONS_DIR +
                        "/" + animationFile.getPath() + "." + meshExt);
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
                    BoneMFModelLoader.loadAdditionalAnimations(model.getModel(), data, name);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                MemoryUtil.memFree(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            HollowCore.LOGGER.error("Trying to load {} additional animations for model: {}, " +
                    "but model wasn't found or it is not animated", getRegistryName(), modelName);
        }


    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return name;
    }

    @Override
    public Class<BTAdditionalAnimationEntry> getRegistryType() {
        return BTAdditionalAnimationEntry.class;
    }
}
