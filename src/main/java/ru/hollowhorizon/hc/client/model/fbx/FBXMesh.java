package ru.hollowhorizon.hc.client.model.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class FBXMesh {
    public final int mode;
    private final FBXVertex[] vertices;
    private final float[] normals;
    private final int[] uvIndices;
    private final int indCount;
    private final int[] indices;
    private final long modelId;
    private final List<FBXCurveNode> animationData = new ArrayList<>();
    private float[] uvMap;
    private FBXMaterial material;

    public FBXMesh(long modelId, double[] vertices, double[] normals, double[] uvMap, int[] uvIndices, int[] indices, int mode) {
        this.vertices = FBXVertex.fromArray(vertices);
        this.normals = toFloatArray(normals);
        this.uvMap = toFloatArray(uvMap);
        this.uvIndices = uvIndices;
        this.indCount = indices.length;
        this.indices = indices;
        this.mode = mode;
        this.modelId = modelId;



        this.material = new FBXMaterial(modelId, "no_material");
        this.material.setMaterialLocation(new ResourceLocation("minecraft:textures/block/dirt.png"));
    }

    public FBXVertex[] getVertices() {
        return vertices;
    }

    public FBXMaterial getMaterial() {
        return material;
    }

    public void addAnimationData(FBXCurveNode node) {
        this.animationData.add(node);
    }

    private float[] toFloatArray(double[] array) {
        int size = array.length;
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            floats[i] = (float) array[i];
        }
        return floats;
    }

    public void clearAnimations() {
        this.animationData.clear();
    }

    public void render(IVertexBuilder builder, MatrixStack stack, int light) {
        stack.pushPose();
        for (FBXCurveNode node : animationData) {
            switch (node.getType()) {
                case SCALING:
                    stack.scale(node.getCurrentX(), node.getCurrentY(), node.getCurrentZ());
                    break;
                case TRANSLATION:
                    stack.translate(node.getCurrentX(), node.getCurrentY(), node.getCurrentZ());
                    break;
                case ROTATION:
                    stack.mulPose(Vector3f.XP.rotationDegrees(node.getCurrentX()));
                    stack.mulPose(Vector3f.YP.rotationDegrees(node.getCurrentY()));
                    stack.mulPose(Vector3f.ZP.rotationDegrees(node.getCurrentZ()));
                    break;
            }
        }

        for (int i = 0; i < indCount; i++) {
            int verIndex = indices[i];
            FBXVertex vertex = vertices[verIndex];

            int n = verIndex * 3;
            int tc = uvIndices[i] * 2;

            builder.vertex(stack.last().pose(), vertex.x, vertex.y, vertex.z)
                    .color(1F, 1F, 1F, 1F)
                    .uv(this.uvMap[tc], this.uvMap[tc+1])
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(light)
                    .normal(stack.last().normal(), normals[n], normals[n + 1], normals[n + 2])
                    .endVertex();
        }
        stack.popPose();
    }

    public long getModelId() {
        return modelId;
    }

    public void addMaterial(FBXMaterial material) {
        this.material = material;
    }

    public float[] getUvMap() {
        return uvMap;
    }

    public void setUvMap(float[] uvMap) {
        this.uvMap = uvMap;
    }
}
