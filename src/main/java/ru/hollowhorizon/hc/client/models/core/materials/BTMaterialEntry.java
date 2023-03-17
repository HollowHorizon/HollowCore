package ru.hollowhorizon.hc.client.models.core.materials;

import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;

public class BTMaterialEntry implements IForgeRegistryEntry<BTMaterialEntry> {
    private ResourceLocation location;
    private ResourceLocation vertexShader;
    private ResourceLocation fragShader;

    public BTMaterialEntry(ResourceLocation vertexShader,
                           ResourceLocation fragShader){
        this.vertexShader = vertexShader;
        this.fragShader = fragShader;
    }

    public ResourceLocation getVertexShader() {
        return vertexShader;
    }

    public ResourceLocation getFragShader() {
        return fragShader;
    }

    @Override
    public BTMaterialEntry setRegistryName(ResourceLocation name) {
        location = name;
        return this;
    }

    public IBTMaterial getProgram(int programId, ShaderLoader vertex, ShaderLoader frag){
        return new BTMaterial(programId, vertex, frag);
    }

    @Nullable
    @Override
    public ResourceLocation getRegistryName() {
        return location;
    }

    @Override
    public Class<BTMaterialEntry> getRegistryType() {
        return BTMaterialEntry.class;
    }
}
