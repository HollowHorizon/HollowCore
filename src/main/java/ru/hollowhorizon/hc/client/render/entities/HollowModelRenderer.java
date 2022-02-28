package ru.hollowhorizon.hc.client.render.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import ru.hollowhorizon.hc.api.utils.IAnimated;
import ru.hollowhorizon.hc.client.model.fbx.FBXModelLoader;
import ru.hollowhorizon.hc.client.model.fbx.FBXModel;

public class HollowModelRenderer<T extends Entity> extends EntityRenderer<T> {
    public final HollowAnimationManager manager;
    public final FBXModel model;

    protected HollowModelRenderer(EntityRendererManager p_i46179_1_, ResourceLocation location) {
        super(p_i46179_1_);
        this.model = FBXModelLoader.createModel(location);
        this.manager = new HollowAnimationManager(model);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return null;
    }

    @Override
    public void render(T entity, float p_225623_2_, float p_225623_3_, MatrixStack stack, IRenderTypeBuffer buffer, int p_225623_6_) {
        super.render(entity, p_225623_2_, p_225623_3_, stack, buffer, p_225623_6_);

        stack.pushPose();
        stack.mulPose(Vector3f.XP.rotationDegrees(-90F));

        if(entity instanceof IAnimated) {
            ((IAnimated) entity).onAnimationUpdate(manager);
        }

        model.updateAnimation(manager);
        model.render(buffer, stack, p_225623_6_);

        stack.popPose();
    }
}
