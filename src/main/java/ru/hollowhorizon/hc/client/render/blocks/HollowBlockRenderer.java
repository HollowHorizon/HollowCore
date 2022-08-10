package ru.hollowhorizon.hc.client.render.blocks;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.api.utils.IAnimated;
import ru.hollowhorizon.hc.client.model.fbx.FBXModel;
import ru.hollowhorizon.hc.client.model.fbx.FBXModelLoader;
import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;

@OnlyIn(Dist.CLIENT)
public class HollowBlockRenderer<T extends TileEntity> extends TileEntityRenderer<T> {
    public final HollowAnimationManager manager;
    public final FBXModel model;

    public HollowBlockRenderer(TileEntityRendererDispatcher tileRenderer, ResourceLocation location) {
        super(tileRenderer);
        this.model = FBXModelLoader.createModel(location);
        this.manager = new HollowAnimationManager(model);
    }

    @Override
    public void render(T tile, float p_225616_2_, MatrixStack stack, IRenderTypeBuffer buffer, int combinedLightIn, int p_225616_6_) {

        stack.pushPose();

        stack.translate(0.5F, 0F, 0.5F);

        stack.mulPose(Vector3f.XP.rotationDegrees(-90F));

        if(tile instanceof IAnimated) {
            ((IAnimated) tile).onAnimationUpdate(manager);
        }


        model.updateAnimation(manager);
        model.render(buffer, stack, getCombinedLight(tile, combinedLightIn));

        stack.popPose();
    }

    private int getCombinedLight(T tileEntityMBE21, int ambientCombinedLight) {

        double playerDistance = 0.0;  // default
        ClientPlayerEntity player = Minecraft.getInstance().player;
        BlockPos blockPos = tileEntityMBE21.getBlockPos();
        if (player != null) {
            Vector3d pedestalCentre = Vector3d.atCenterOf(blockPos).add(0.5, 1.0, 0.5);
            Vector3d playerFeet = player.position();
            playerDistance = playerFeet.distanceTo(pedestalCentre);
        }

        final double DISTANCE_FOR_MIN_GLOW = 8.0;
        final double DISTANCE_FOR_MAX_GLOW = 6.0;
        final double MIN_GLOW = 0.0;
        final double MAX_GLOW = 1.0;
        double glowMultiplier = interpolate_with_clipping(playerDistance, DISTANCE_FOR_MIN_GLOW, DISTANCE_FOR_MAX_GLOW,
                MIN_GLOW, MAX_GLOW);

        final int SKY_LIGHT_VALUE = (int)(15 * glowMultiplier);
        final int BLOCK_LIGHT_VALUE = (int)(15 * glowMultiplier);

        return LightTexture.pack(BLOCK_LIGHT_VALUE, SKY_LIGHT_VALUE);
    }

    public static double interpolate_with_clipping(double x, double x1, double x2, double y1, double y2)
    {
        if (x1 > x2) {
            double temp = x1; x1 = x2; x2 = temp;
            temp = y1; y1 = y2; y2 = temp;
        }

        if (x <= x1) return y1;
        if (x >= x2) return y2;
        double xFraction = (x - x1) / (x2 - x1);
        return y1 + xFraction * (y2 - y1);
    }
}
