package ru.hollowhorizon.hc.client.models.core.materials;

import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.util.ResourceLocation;

public class AnimatedMaterialEntry extends BTMaterialEntry {

    public AnimatedMaterialEntry(ResourceLocation vertexShader,
                                 ResourceLocation fragShader) {
        super(vertexShader, fragShader);
    }

    @Override
    public IBTMaterial getProgram(int programId, ShaderLoader vertex, ShaderLoader frag) {
        return new AnimatedMaterial(programId, vertex, frag);
    }
}
