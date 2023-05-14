package ru.hollowhorizon.hc.client.gltf;

import org.lwjgl.opengl.*;

public class RenderedGltfSceneGL33 extends RenderedGltfScene {

    @Override
    public void renderForVanilla() {
        if(!skinningCommands.isEmpty()) {
            GL20.glUseProgram(GlTFModelManager.getInstance().getGlProgramSkinning());
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            skinningCommands.forEach(Runnable::run);
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
            GL20.glUseProgram(0);
        }

        vanillaRenderCommands.forEach(Runnable::run);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
    }

    @Override
    public void renderForShaderMod() {
        if(!skinningCommands.isEmpty()) {
            int currentProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL20.glUseProgram(GlTFModelManager.getInstance().getGlProgramSkinning());
            GL11.glEnable(GL30.GL_RASTERIZER_DISCARD);
            skinningCommands.forEach(Runnable::run);
            GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0);
            GL11.glDisable(GL30.GL_RASTERIZER_DISCARD);
            GL20.glUseProgram(currentProgram);
        }

        shaderModRenderCommands.forEach(Runnable::run);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
        RenderedGltfModel.NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear();
    }

}