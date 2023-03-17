package ru.hollowhorizon.hc.client.models.core.materials;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.GlobalRenderInfo;
import ru.hollowhorizon.hc.client.models.core.animation.IPose;

import java.util.ArrayList;
import java.util.List;


public class BTMaterial implements IBTMaterial {
    protected final int program;
    protected final ShaderLoader vert;
    protected final ShaderLoader frag;
    protected boolean firstUpload;
    protected final static int DIFFUSE_COUNT = 2;
    public MaterialUniform projUniform;
    public MaterialUniform modelViewUniform;
    public MaterialUniform lightMapUV;
    public MaterialUniform overlayUV;
    public MaterialUniform ambientLight;
    public MaterialUniform diffuseColors;
    public MaterialUniform diffuseLocs;
    protected final List<MaterialUniform> uniforms = new ArrayList<>();

    public BTMaterial(int program, ShaderLoader vert, ShaderLoader frag) {
        this.program = program;
        this.vert = vert;
        this.frag = frag;
        this.firstUpload = true;
        modelViewUniform = new MaterialUniform("model_view",
                MaterialUniform.UniformType.mat4x4, this);
        uniforms.add(modelViewUniform);
        projUniform = new MaterialUniform("proj_mat",
                MaterialUniform.UniformType.mat4x4, this);
        uniforms.add(projUniform);
        lightMapUV = new MaterialUniform("lightmap_uv",
                MaterialUniform.UniformType.vec2f, this);
        uniforms.add(lightMapUV);
        overlayUV = new MaterialUniform("overlay_uv",
                MaterialUniform.UniformType.vec2f, this);
        uniforms.add(overlayUV);
        ambientLight = new MaterialUniform("ambient_light",
                MaterialUniform.UniformType.vec3f,this);
        uniforms.add(ambientLight);
        diffuseColors = new MaterialUniform("diffuse_colors",
                MaterialUniform.UniformType.vec3f, DIFFUSE_COUNT, this);
        uniforms.add(diffuseColors);
        diffuseLocs = new MaterialUniform("diffuse_locs",
                MaterialUniform.UniformType.vec3f, DIFFUSE_COUNT, this);
        uniforms.add(diffuseLocs);

    }

    @Override
    public void initRender(RenderType renderType, MatrixStack matrixStackIn, Matrix4f projection,
                           int packedLight, int packedOverlay){

        this.useProgram();
        renderType.setupRenderState();
        if (this.firstUpload){
            initStaticValues();
            this.firstUpload = false;
        }
        this.uploadModelViewMatrix(matrixStackIn.last().pose());
        this.uploadProjectionMatrix(projection);
        this.uploadPackedLightMap(packedLight);
        this.uploadPackedOverlay(packedOverlay);
        this.receiveLightingInfo(GlobalRenderInfo.currentFrameGlobal);

    }

    public void initStaticValues(){
        ambientLight.set(0.4f, 0.4f, 0.4f);
        ambientLight.upload();
        diffuseColors.set(0.6f, 0.6f, 0.6f, 0.6f, 0.6f, 0.6f);
        diffuseColors.upload();
    }

    @Override
    public void endRender(RenderType renderType){
        renderType.clearRenderState();
        this.releaseProgram();
    }

    @Override
    public int getId() {
        return program;
    }

    @Override
    public void markDirty() {

    }

    @Override
    public void setupUniforms(){
        for (MaterialUniform uniform : uniforms){
            uniform.bindUniform(program);
        }
    }

    @Override
    public void uploadAnimationFrame(IPose pose) {
        HollowCore.LOGGER.warn("BTShaderProgram does not support animation, skipping uploadAnimationFrame. " +
                "Use AnimatedShaderProgram instead.");
    }

    @Override
    public void uploadInverseBindPose(IPose pose) {
        HollowCore.LOGGER.warn("BTShaderProgram does not support animation, skipping uploadInverseBindPose. " +
                "Use AnimatedShaderProgram instead.");
    }


    public void useProgram(){
        ShaderLinkHelper.glUseProgram(program);
    }

    public void releaseProgram(){
        ShaderLinkHelper.glUseProgram(0);
    }

    public void uploadProjectionMatrix(Matrix4f mat){
        projUniform.set(mat);
        projUniform.upload();
    }

    public void uploadModelViewMatrix(Matrix4f mat){
        modelViewUniform.set(mat);
        modelViewUniform.upload();
    }

    public void uploadPackedLightMap(int packedLightmap){
        lightMapUV.set(((packedLightmap & 0xFFFF) / 256.0f) + 0.03125f, ((packedLightmap >>> 16) / 256.0f) + 0.03125f);
        lightMapUV.upload();
    }

    public void uploadPackedOverlay(int packedOverlay){
        overlayUV.set((short)(packedOverlay & '\uffff') / 16.0f, (short)(packedOverlay >>> 16 & '\uffff') / 16.0f);
        overlayUV.upload();
    }


    public void receiveLightingInfo(MatrixStack lightingStack){
        Vector4f diffuse0Loc = new Vector4f(GlobalRenderInfo.DIFFUSE_LIGHT_0);
        diffuse0Loc.transform(lightingStack.last().pose());
        Vector4f diffuse1Loc = new Vector4f(GlobalRenderInfo.DIFFUSE_LIGHT_1);
        diffuse1Loc.transform(lightingStack.last().pose());

        diffuseLocs.set(diffuse0Loc.x(), diffuse0Loc.y(), diffuse0Loc.z(),
                diffuse1Loc.x(), diffuse1Loc.y(), diffuse1Loc.z());
        diffuseLocs.upload();

    }

    @Override
    public ShaderLoader getVertexProgram() {
        return vert;
    }

    @Override
    public ShaderLoader getFragmentProgram() {
        return frag;
    }
}