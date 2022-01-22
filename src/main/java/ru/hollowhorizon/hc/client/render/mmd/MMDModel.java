package ru.hollowhorizon.hc.client.render.mmd;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.render.models.IHollowModel;
import ru.hollowhorizon.hc.dll.HollowRenderManager;

import java.nio.ByteBuffer;

public class MMDModel implements IHollowModel {
    private static HollowRenderManager hollowRenderer;
    long model;
    String modelDir;
    int vertexCount;
    ByteBuffer posBuffer, norBuffer, uvBuffer;
    int ibo;
    int indexElementSize;
    int indexType;
    Material[] mats;

    private MMDModel() {

    }

    public static MMDModel create(String modelFilename, String modelDir, boolean isPMD, long layerCount) {
        if (hollowRenderer == null) hollowRenderer = HollowRenderManager.getInstance();
        long model;
        if (isPMD)
            model = hollowRenderer.LoadModelPMD(modelFilename, modelDir, layerCount);
        else {
            model = hollowRenderer.LoadModelPMX(modelFilename, modelDir, layerCount);
        }
        if (model == 0) {
            HollowCore.LOGGER.info(String.format("Cannot open model: '%s'.", modelFilename));
            return null;
        }

        int vertexCount = (int) hollowRenderer.GetVertexCount(model);
        ByteBuffer posBuffer = ByteBuffer.allocateDirect(vertexCount * 12); //float * 3
        ByteBuffer norBuffer = ByteBuffer.allocateDirect(vertexCount * 12);
        ByteBuffer uvBuffer = ByteBuffer.allocateDirect(vertexCount * 8); //float * 2

        int indexElementSize = (int) hollowRenderer.GetIndexElementSize(model);
        int indexCount = (int) hollowRenderer.GetIndexCount(model);
        int indexSize = indexCount * indexElementSize;
        long indexData = hollowRenderer.GetIndices(model);
        int ibo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        ByteBuffer indexBuffer = ByteBuffer.allocateDirect(indexSize);
        for (int i = 0; i < indexSize; ++i)
            indexBuffer.put(hollowRenderer.ReadByte(indexData, i));
        indexBuffer.position(0);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        int indexType = 0;
        switch (indexElementSize) {
            case 1:
                indexType = GL11.GL_UNSIGNED_BYTE;
                break;
            case 2:
                indexType = GL11.GL_UNSIGNED_SHORT;
                break;
            case 4:
                indexType = GL11.GL_UNSIGNED_INT;
                break;
        }

        MMDModel.Material[] mats = new MMDModel.Material[(int) hollowRenderer.GetMaterialCount(model)];
        for (int i = 0; i < mats.length; ++i) {
            mats[i] = new MMDModel.Material();
            String texFilename = hollowRenderer.GetMaterialTex(model, i);
            if (!texFilename.isEmpty()) {
                MMDTextureManager.Texture mgrTex = MMDTextureManager.getTexture(texFilename);
                if (mgrTex != null) {
                    mats[i].tex = mgrTex.tex;
                    mats[i].hasAlpha = mgrTex.hasAlpha;
                }
            }
        }

        MMDModel result = new MMDModel();
        result.model = model;
        result.modelDir = modelDir;
        result.vertexCount = vertexCount;
        result.posBuffer = posBuffer;
        result.norBuffer = norBuffer;
        result.uvBuffer = uvBuffer;
        result.ibo = ibo;
        result.indexElementSize = indexElementSize;
        result.indexType = indexType;
        result.mats = mats;
        return result;
    }

    public static void delete(MMDModel model) {
        hollowRenderer.DeleteModel(model.model);
    }

    public void render(IRenderTypeBuffer buffer, float entityYaw, Matrix4f mat, int packedLight) {
        update();
        renderModel(buffer, entityYaw, mat, packedLight);
    }

    public void changeAnim(long anim, long layer) {
        hollowRenderer.ChangeModelAnim(model, anim, layer);
    }

    public void resetPhysics() {
        hollowRenderer.ResetModelPhysics(model);
    }

    public long getModelLong() {
        return model;
    }

    public String getModelDir() {
        return modelDir;
    }

    private void update() {
        hollowRenderer.UpdateModel(model);
    }

    private void renderModel(IRenderTypeBuffer buffer, float entityYaw, Matrix4f mat, int packedLight) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        RenderSystem.pushMatrix();

        RenderSystem.multMatrix(mat);


        //RenderSystem.rotatef(180f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(-entityYaw, 0.0f, 1.0f, 0.0f);
        RenderSystem.scaled(0.1, 0.1, 0.1);


        int posAndNorSize = vertexCount * 12;
        long posData = hollowRenderer.GetPoss(model);
        hollowRenderer.CopyDataToByteBuffer(posBuffer, posData, posAndNorSize);
        long norData = hollowRenderer.GetNormals(model);
        hollowRenderer.CopyDataToByteBuffer(norBuffer, norData, posAndNorSize);
        int uvSize = vertexCount * 8; //float * 2
        long uvData = hollowRenderer.GetUVs(model);
        hollowRenderer.CopyDataToByteBuffer(uvBuffer, uvData, uvSize);

        //Init vertex pointer
        GL11.glVertexPointer(3, GL11.GL_FLOAT, 0, posBuffer);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glNormalPointer(GL11.GL_FLOAT, 0, norBuffer);
        GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
        GL13.glClientActiveTexture(GL13.GL_TEXTURE0);
        GL11.glTexCoordPointer(2, GL11.GL_FLOAT, 0, uvBuffer);
        GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);

        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        int j = packedLight % 65536;
        int k = packedLight / 65536;
        GL13.glMultiTexCoord2f(33986, (float) j, (float) k);

        //Render
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ibo);
        long subMeshCount = hollowRenderer.GetSubMeshCount(model);
        for (long i = 0; i < subMeshCount; ++i) {
            int materialID = hollowRenderer.GetSubMeshMaterialID(model, i);
            float alpha = hollowRenderer.GetMaterialAlpha(model, materialID);
            if (alpha == 0.0f)
                continue;

            if (hollowRenderer.GetMaterialBothFace(model, materialID)) {
                RenderSystem.disableCull();
            } else {
                RenderSystem.enableCull();
            }

            if (mats[materialID].tex == 0)
                Minecraft.getInstance().getTextureManager().bind(TextureManager.INTENTIONAL_MISSING_TEXTURE);
            else
                RenderSystem.bindTexture(mats[materialID].tex);
            long startPos = (long) hollowRenderer.GetSubMeshBeginIndex(model, i) * indexElementSize;
            int count = hollowRenderer.GetSubMeshVertexCount(model, i);
            GL11.glDrawElements(GL11.GL_TRIANGLES, count, indexType, startPos);
        }
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);


        GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
        GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

        RenderSystem.enableCull();
        RenderSystem.popMatrix();
        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
    }

    static class Material {
        int tex;
        boolean hasAlpha;

        Material() {
            tex = 0;
            hasAlpha = false;
        }
    }
}
