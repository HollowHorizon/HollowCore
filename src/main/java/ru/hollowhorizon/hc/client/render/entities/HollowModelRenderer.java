package ru.hollowhorizon.hc.client.render.entities;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.model.smd.IncrementingVariable;
import ru.hollowhorizon.hc.client.model.smd.SmdAnimation;
import ru.hollowhorizon.hc.client.model.smd.SmdAnimationSequence;
import ru.hollowhorizon.hc.client.model.smd.ValveStudioModel;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowModelRenderer<T extends Entity> extends EntityRenderer<T> {
    public static final RenderType.State state = RenderType.State.builder().setShadeModelState(new RenderState.ShadeModelState(true)).setTextureState(new RenderState.TextureState(TextureManager.INTENTIONAL_MISSING_TEXTURE, false, false)).setTransparencyState(new RenderState.TransparencyState("translucent_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            })).setDiffuseLightingState(new RenderState.DiffuseLightingState(true))
            .setAlphaState(new RenderState.AlphaState(0.003921569F)).setCullState(RenderType.NO_CULL).setLightmapState(new RenderState.LightmapState(true)).setOverlayState(new RenderState.OverlayState(true)).createCompositeState(false);
    public final ValveStudioModel model;
    private IncrementingVariable variable = null;

    protected HollowModelRenderer(EntityRendererManager p_i46179_1_) {
        super(p_i46179_1_);
        this.model = new ValveStudioModel(new ResourceLocation(MODID, "models/cyborg/cyborg.pqc"));
        this.variable = new IncrementingVariable(1.0F, model.currentSequence.current().totalFrames-1);
        HollowCore.LOGGER.info("end model");
    }

    public static RenderType getSMDRenderType() {
        return RenderType.create("entity_smd", DefaultVertexFormats.NEW_ENTITY, 4, 1024, true, false, state);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_110775_1_) {
        return new ResourceLocation(MODID, "textures/entity/diffuse.png");
    }

    protected boolean func_225622_a_(T livingEntityIn) {
        return !livingEntityIn.isInvisible();
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack stack, IRenderTypeBuffer buffer, int packedLightIn) {
        super.render(entity, entityYaw, partialTicks, stack, buffer, packedLightIn);
        RenderType rendertype = getSMDRenderType();
        int packedOverlay = LivingRenderer.getOverlayCoords((LivingEntity) entity, 0.0F);

        stack.pushPose();
        stack.mulPose(Vector3f.YP.rotationDegrees(entityYaw));
        stack.mulPose(Vector3f.XP.rotationDegrees(90F));

        this.variable.tick();
        updateAnimation(this.variable);

        model.renderToBuffer(stack, buffer.getBuffer(rendertype), packedLightIn, packedOverlay, 1F, 1F, 1F, 1F);
        stack.popPose();
    }

    protected void updateAnimation(IncrementingVariable variable) {
        SmdAnimationSequence sequence = this.model.currentSequence;
        if (sequence == null || variable == null) {
            System.out.println("ERROR IN " + this.model.resource.toString());
        }

        SmdAnimation animation = sequence.checkForIncrement(variable);
        int frame = (int)Math.floor(variable.value % (float)animation.totalFrames);
        if (variable.shouldReverse && variable.value >= (float)animation.totalFrames) {
            variable.inReverse = true;
            if (variable.shouldStayAtEnd) {
                variable.setAtEnd(true);
            }

            frame = animation.totalFrames - 1;
        }

        animation.setCurrentFrame(MathHelper.clamp(frame, 0, animation.totalFrames - 1));

        this.model.animate();
    }
}
