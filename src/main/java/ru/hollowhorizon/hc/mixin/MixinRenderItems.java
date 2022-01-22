package ru.hollowhorizon.hc.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.tools.HollowItemColors;

import java.util.List;

@Mixin(ItemRenderer.class)
public abstract class MixinRenderItems {

    @Inject(method = "render", at = @At("HEAD"))
    private void setColorRuneTargetStack(ItemStack itemStackIn, ItemCameraTransforms.TransformType p_229111_2_, boolean p_229111_3_, MatrixStack p_229111_4_, IRenderTypeBuffer p_229111_5_, int p_229111_6_, int p_229111_7_, IBakedModel p_229111_8_, CallbackInfo ci) {
        HollowItemColors.setTargetStack(itemStackIn);
    }

    @Redirect(method = "getArmorFoilBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorGlint()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getArmorGlint() {
        return HollowItemColors.getArmorGlint();
    }

    @Redirect(method = "getArmorFoilBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;armorEntityGlint()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getArmorEntityGlint() {
        return HollowItemColors.getArmorEntityGlint();
    }

    @Redirect(method = "getFoilBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;glint()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getGlint() {
        return HollowItemColors.getGlint();
    }

    @Redirect(method = "getFoilBuffer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityGlint()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getEntityGlint() {
        return HollowItemColors.getEntityGlint();
    }

    @Redirect(method = "getFoilBufferDirect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;glintDirect()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getGlintDirect() {
        return HollowItemColors.getGlintDirect();
    }

    @Redirect(method = "getFoilBufferDirect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/RenderType;entityGlintDirect()Lnet/minecraft/client/renderer/RenderType;"))
    private static RenderType getEntityGlintDirect() {
        return HollowItemColors.getEntityGlintDirect();
    }
}
