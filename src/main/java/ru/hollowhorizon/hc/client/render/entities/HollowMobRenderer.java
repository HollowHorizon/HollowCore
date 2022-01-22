package ru.hollowhorizon.hc.client.render.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Quaternion;
import ru.hollowhorizon.hc.api.utils.IAnimatedEntity;
import ru.hollowhorizon.hc.client.render.mmd.MMDAnimManager;
import ru.hollowhorizon.hc.client.render.mmd.MMDModelManager;

public class HollowMobRenderer<T extends Entity> extends EntityRenderer<T> {
    private final String modelName;
    private String currentAnim = "";

    public HollowMobRenderer(EntityRendererManager manager, String model) {
        super(manager);
        this.modelName = model;
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return null;
    }

    @Override
    public boolean shouldRender(T livingEntityIn, ClippingHelper camera, double camX, double camY, double camZ) {
        MMDModelManager.update();

        if (!livingEntityIn.shouldRender(camX, camY, camZ)) {
            return false;
        } else if (livingEntityIn.noCulling) {
            return true;
        } else {
            AxisAlignedBB axisalignedbb = livingEntityIn.getBoundingBoxForCulling().inflate(0.5D);
            if (axisalignedbb.hasNaN() || axisalignedbb.getSize() == 0.0D) {
                axisalignedbb = new AxisAlignedBB(livingEntityIn.getX() - 2.0D, livingEntityIn.getY() - 2.0D, livingEntityIn.getZ() - 2.0D, livingEntityIn.getX() + 2.0D, livingEntityIn.getY() + 2.0D, livingEntityIn.getZ() + 2.0D);
            }

            return camera.isVisible(axisalignedbb);
        }
    }

    @Override
    public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);

        MMDModelManager.Model model = MMDModelManager.getModelOrInPool(entityIn, modelName, false);

        if (model != null) {
            if(entityIn instanceof IAnimatedEntity) {
                IAnimatedEntity animatedEntity = (IAnimatedEntity) entityIn;

                animatedEntity.processAnimation();

                HollowAnimationManager manager = animatedEntity.getManager();

                playAnimation(model, manager.getAnimName());
            }

            model.unuseTime = 0;
            matrixStackIn.pushPose();
            model.model.render(bufferIn, entityYaw, matrixStackIn.last().pose(), packedLightIn);
            matrixStackIn.popPose();
        }
    }

    void playAnimation(MMDModelManager.Model model, String animName) {
        if (!currentAnim.equals(animName)) {
            currentAnim = animName;
            model.model.changeAnim(MMDAnimManager.getAnimModel(model.model, animName), 0);
        }
    }
}
