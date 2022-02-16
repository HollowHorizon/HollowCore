package ru.hollowhorizon.hc.client.render.mmd;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;

public interface IHollowModel {
    void resetPhysics();
    void changeAnim(long model, long layer);
    void render(IRenderTypeBuffer buffer, float entityYaw, Matrix4f mat, int packedLight);

    String getModelDir();

    long getModelLong();
}
