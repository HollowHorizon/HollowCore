package ru.hollowhorizon.hc.client.models.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.commons.Method;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXFile;
import ru.hollowhorizon.hc.client.models.fbx.raw.Vertex;

public class FBXModel {
    private final FBXFile file;
    private FBXGeometry fbxGeometry;
    private FBXSkeleton fbxSkeleton;

    public FBXModel(FBXFile file) {
        this.file = file;

        FBXElement model = this.file.getChild(0);

        FBXElement geometry = this.file.getChild(model.getId());

        loadGeometry(geometry);

        loadSkeleton(findRootBone());
        loadSkeletonDeformer();

        fbxSkeleton.print();
    }

    private static float[] toFloatArray(double[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }

    public FBXGeometry getFbxGeometry() {
        return fbxGeometry;
    }

    private void loadSkeletonDeformer() {
        loadBoneDeformer(this.fbxSkeleton.getRoot());
    }

    private void loadBoneDeformer(FBXBone bone) {
        FBXElement[] elements = this.file.getParents(bone.getId());

        for (FBXElement element : elements) {
            if (element.getName().equals("Deformer")) {
                bone.applyDeformer(element);
            }
        }

        for (FBXBone cbone : bone.getChildren()) {
            loadBoneDeformer(cbone);
        }
    }

    private FBXBone findRootBone() {
        FBXElement[] elements = this.file.getChildren(0);

        for (FBXElement element : elements) {
            if (element.getProperties()[2].getData().equals("Null")) {
                return new FBXBone(element.getPName(), element.getId());
            }
        }

        throw new IllegalStateException("No root bone found");
    }

    private void loadSkeleton(FBXBone rootBone) {
        loadBone(rootBone);

        fbxSkeleton = new FBXSkeleton(this.file, rootBone);
    }

    public FBXBone loadBone(FBXBone parent) {
        for (FBXElement element : this.file.getChildren(parent.getId())) {
            if (element.getName().equals("NodeAttribute")) continue;

            if (element.getProperties()[2].getData().equals("LimbNode")) {
                String name = element.getPName().split("\u0000\u0001")[0];

                if (name.endsWith("_end")) continue;

                FBXBone bone = new FBXBone(name, element.getId());
                bone.setParent(parent);
                parent.addChild(bone);
                loadBone(bone);
            }
        }
        return parent;
    }

    private void loadGeometry(FBXElement geometry) {


        float[] verticesArray = toFloatArray(geometry.getFirstElement("Vertices").getProperties()[0].getData());

        float[] normals = toFloatArray(geometry.getFirstElement("LayerElementNormal").getFirstElement("Normals").getProperties()[0].getData());
        float[] uvs = toFloatArray(geometry.getFirstElement("LayerElementUV").getFirstElement("UV").getProperties()[0].getData());

        int[] indices = geometry.getFirstElement("PolygonVertexIndex").getProperties()[0].getData();
        int[] uvIndices = geometry.getFirstElement("LayerElementUV").getFirstElement("UVIndex").getProperties()[0].getData();
        int l = indices.length;

        Vertex[] vertexes = new Vertex[l];

        int faceSize = 0;

        for (int i = 0; i < indices.length; ++i) {
            if (indices[i] < 0) {
                indices[i] = -indices[i] - 1;
                if (faceSize == 0) faceSize = i + 1;
            }
        }

        for (int i = 0; i < l; ++i) {
            int iV = indices[i] * 3;
            int iT = uvIndices[i] * 2;
            float x = verticesArray[iV];
            float y = verticesArray[iV + 1];
            float z = verticesArray[iV + 2];
            float nx = normals[iV];
            float ny = normals[iV + 1];
            float nz = normals[iV + 2];
            float u = uvs[iT];
            float v = uvs[iT + 1];

            vertexes[i] = new Vertex(i, x, y, z, nx, ny, nz, u, v);
        }

        this.fbxGeometry = new FBXGeometry(vertexes);
    }

    public void render(MatrixStack stack, IRenderTypeBuffer buffers, int light, int overlay) {
        this.fbxSkeleton.update(new MatrixStack());

        this.fbxGeometry.render(this.fbxSkeleton, stack, buffers.getBuffer(RenderType.entityCutout(TextureManager.INTENTIONAL_MISSING_TEXTURE)), light, overlay);

    }


    public FBXSkeleton getSkeleton() {
        return fbxSkeleton;
    }
}
