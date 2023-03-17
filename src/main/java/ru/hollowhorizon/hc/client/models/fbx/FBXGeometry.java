package ru.hollowhorizon.hc.client.models.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import ru.hollowhorizon.hc.client.models.fbx.raw.Vertex;

public class FBXGeometry {
    private final Vertex[] vertexes;

    public FBXGeometry(Vertex[] vertexes) {
        this.vertexes = vertexes;
    }

    public void render(FBXSkeleton skeleton, MatrixStack stack, IVertexBuilder builder, int light, int overlay) {
        Matrix4f matrix = stack.last().pose();
        Matrix3f normalMatrix = stack.last().normal();

        for (Vertex vertex : vertexes) {
            vertex.draw(skeleton, matrix, normalMatrix, builder, light, overlay);
        }
    }

    public Vertex[] getVertexes() {
        return vertexes;
    }

    public Vertex vertex(int id) {
        return vertexes[id];
    }
}
