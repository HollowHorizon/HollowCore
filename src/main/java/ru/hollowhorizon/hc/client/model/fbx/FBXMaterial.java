package ru.hollowhorizon.hc.client.model.fbx;

import net.minecraft.util.ResourceLocation;

public class FBXMaterial {
    private final long modelId;
    private final String materialName;
    private ResourceLocation materialLocation;

    public FBXMaterial(long modelId, String materialName) {
        this.modelId = modelId;
        this.materialName = materialName;
    }

    public long getModelId() {
        return modelId;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialLocation(ResourceLocation materialLocation) {
        this.materialLocation = materialLocation;
    }

    public ResourceLocation getTexture() {
        return materialLocation;
    }
}
