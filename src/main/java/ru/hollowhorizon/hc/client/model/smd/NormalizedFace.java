package ru.hollowhorizon.hc.client.model.smd;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class NormalizedFace {
    public DeformVertex[] vertices;
    public TextureCoordinate[] textureCoordinates;
    public Vertex faceNormal;

    public NormalizedFace(DeformVertex[] xyz, TextureCoordinate[] uvs) {
        this.vertices = xyz;
        this.textureCoordinates = uvs;
    }

    public NormalizedFace(NormalizedFace face, List<DeformVertex> verts) {
        this.vertices = new DeformVertex[face.vertices.length];

        for(int i = 0; i < this.vertices.length; ++i) {
            this.vertices[i] = verts.get(face.vertices[i].ID);
        }

        this.textureCoordinates = new TextureCoordinate[face.textureCoordinates.length];
        System.arraycopy(face.textureCoordinates, 0, this.textureCoordinates, 0, this.textureCoordinates.length);
        if (face.faceNormal != null) {
            this.faceNormal = face.faceNormal;
        }

    }

    public Vertex calculateFaceNormal() {
        Vector3d v1 = new Vector3d((this.vertices[1].x - this.vertices[0].x), (this.vertices[1].y - this.vertices[0].y), (this.vertices[1].z - this.vertices[0].z));
        Vector3d v2 = new Vector3d(this.vertices[2].x - this.vertices[0].x, (this.vertices[2].y - this.vertices[0].y), (this.vertices[2].z - this.vertices[0].z));
        Vector3d normalVector;
        normalVector = v1.cross(v2).normalize();
        return new Vertex((float)normalVector.x, (float)normalVector.y, (float)normalVector.z);
    }

    public void addFaceForRender(MatrixStack matrixStack, BufferBuilder bufferBuilder, int packedLight, int packedOverlay, boolean smoothShading, float partialTick, float r, float g, float b, float a) {
        Matrix4f poseMatrix = matrixStack.last().pose();
        Matrix3f normalMatrix = matrixStack.last().normal();
        if (!smoothShading && this.faceNormal == null) {
            this.faceNormal = this.calculateFaceNormal();
        }

        for(int i = 0; i < 3; ++i) {
            IVertexBuilder vertexBuilder = bufferBuilder.vertex(poseMatrix, this.vertices[i].getX(partialTick), this.vertices[i].getY(partialTick), this.vertices[i].getZ(partialTick)).color(r, g, b, a).uv(this.textureCoordinates[i].u, this.textureCoordinates[i].v).overlayCoords(packedOverlay).uv2(packedLight);
            if (!smoothShading) {
                vertexBuilder.normal(normalMatrix, this.faceNormal.x, this.faceNormal.y, this.faceNormal.z);
            } else {
                vertexBuilder.normal(normalMatrix, this.vertices[i].getXN(partialTick), this.vertices[i].getYN(partialTick), this.vertices[i].getZN(partialTick));
            }

            vertexBuilder.endVertex();
        }

    }
}
