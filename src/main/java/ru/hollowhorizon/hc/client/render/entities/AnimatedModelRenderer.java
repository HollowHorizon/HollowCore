package ru.hollowhorizon.hc.client.render.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.common.registry.ModModels;
import ru.hollowhorizon.hc.proxy.ClientProxy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class AnimatedModelRenderer<T extends LivingEntity> extends LivingRenderer<T, EntityModel<T>> {

    public AnimatedModelRenderer(EntityRendererManager entityRendererManager) {
        super(entityRendererManager, new EntityModel<T>() {
            @Override
            public void setupAnim(T p_225597_1_, float p_225597_2_, float p_225597_3_, float p_225597_4_, float p_225597_5_, float p_225597_6_) {
            }

            @Override
            public void renderToBuffer(MatrixStack p_225598_1_, IVertexBuilder p_225598_2_, int p_225598_3_, int p_225598_4_, float p_225598_5_, float p_225598_6_, float p_225598_7_, float p_225598_8_) {
            }
        }, 2);


    }

    @Nullable
    protected RenderType getRenderType(T entityType, boolean isVisible, boolean visibleToPlayer) {
        ResourceLocation resourcelocation = this.getTextureLocation(entityType);
        if (visibleToPlayer) {
            return RenderType.entityTranslucent(resourcelocation);
        } else if (isVisible) {
            return RenderType.entityTranslucent(resourcelocation);
        } else {
            return entityType.isGlowing() ? RenderType.outline(resourcelocation) : null;
        }
    }

    protected boolean func_225622_a_(T livingEntityIn) {
        return !livingEntityIn.isInvisible();
    }

    public void render(@Nonnull T entityIn, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int packedLightIn) {
        boolean visible = this.func_225622_a_(entityIn);
        boolean visibleToPlayer = !visible && !entityIn.isInvisibleTo(Objects.requireNonNull(Minecraft.getInstance().player));
        RenderType rendertype = this.getRenderType(entityIn, visible, visibleToPlayer);
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        Matrix4f projMatrix = gameRenderer.getProjectionMatrix(gameRenderer.getMainCamera(), partialTicks, true);
        int packedOverlay = LivingRenderer.getOverlayCoords(entityIn, partialTicks);
        this.drawModel(rendertype, entityIn, matrixStackIn, projMatrix, packedLightIn, packedOverlay, entityYaw);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return new ResourceLocation(MODID, "textures/entity/diffuse.png");
    }

    public void drawModel(RenderType renderType, T entityIn, MatrixStack matrixStackIn, Matrix4f projectionMatrix, int packedLightIn, int packedOverlay, float entityYaw) {
        RenderSystem.pushMatrix();
        RenderSystem.rotatef(-entityYaw, 0.0F, 1.0F, 0.0F);

        matrixStackIn.pushPose();

        ModModels.TEST.getAnimator().onUpdate();

        ModModels.TEST.getAnimator().onPreRender();
        ClientProxy.SHADER.start();
        renderType.setupRenderState();
        ClientProxy.SHADER.getModelViewMatrix().load(matrixStackIn.last().pose());
        ClientProxy.SHADER.getProjectionMatrix().load(projectionMatrix);
        ClientProxy.SHADER.getLightMapTextureCoords().load((float) (packedLightIn & '\uffff') / 256.0F + 0.03125F, (float) (packedLightIn >>> 16) / 256.0F + 0.03125F);
        ClientProxy.SHADER.getOverlayTextureCoords().load((float) ((short) (packedOverlay & '\uffff')) / 16.0F, (float) ((short) (packedOverlay >>> 16 & '\uffff')) / 16.0F);
        ClientProxy.SHADER.getJointTransforms().load(ModModels.TEST.getSkeleton().getAnimationModelSpaceTransformsFromJoints());
        ModModels.TEST.getSkin().getSkinModel().bind(0, 1, 2, 3, 4);
        GL11.glDrawElements(4, ModModels.TEST.getSkin().getSkinModel().getIndexCount(), 5125, 0L);
        ModModels.TEST.getSkin().getSkinModel().unbind(0, 1, 2, 3, 4);
        renderType.clearRenderState();
        ClientProxy.SHADER.stop();

        matrixStackIn.popPose();
        RenderSystem.popMatrix();
    }
}