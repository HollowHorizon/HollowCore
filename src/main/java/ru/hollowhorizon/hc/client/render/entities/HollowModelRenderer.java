package ru.hollowhorizon.hc.client.render.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import ru.hollowhorizon.hc.client.model.fbx.FBXLoader;
import ru.hollowhorizon.hc.client.model.fbx.FBXModel;

public class HollowModelRenderer<T extends Entity> extends EntityRenderer<T> {
    public final FBXModel model;

    protected HollowModelRenderer(EntityRendererManager p_i46179_1_, ResourceLocation location) {
        super(p_i46179_1_);
        this.model = FBXLoader.createModel(location);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return null;
    }

    @Override
    public void render(T p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack stack, IRenderTypeBuffer buffer, int p_225623_6_) {
        super.render(p_225623_1_, p_225623_2_, p_225623_3_, stack, buffer, p_225623_6_);

        stack.pushPose();
        stack.mulPose(Vector3f.XP.rotationDegrees(-90F));

        model.updateAnimation();
        model.render(buffer, stack, p_225623_6_);

        stack.popPose();
    }
}
