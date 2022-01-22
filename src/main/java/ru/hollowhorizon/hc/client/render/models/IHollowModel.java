package ru.hollowhorizon.hc.client.render.models;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Matrix4f;

public interface IHollowModel {
    void render(IRenderTypeBuffer bufferIn, float entityYaw, Matrix4f mat, int packedLight);

    void changeAnim(long anim, long layer);

    void resetPhysics();

    long getModelLong();

    String getModelDir();
}
