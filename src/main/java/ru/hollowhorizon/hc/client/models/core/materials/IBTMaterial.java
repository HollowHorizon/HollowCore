package ru.hollowhorizon.hc.client.models.core.materials;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.util.math.vector.Matrix4f;
import ru.hollowhorizon.hc.client.models.core.animation.IPose;

public interface IBTMaterial extends IShaderManager {
    void initRender(RenderType renderType, MatrixStack matrixStackIn, Matrix4f projection,
                    int packedLight, int packedOverlay);

    void endRender(RenderType renderType);

    void setupUniforms();

    void uploadAnimationFrame(IPose pose);

    void uploadInverseBindPose(IPose pose);
}
