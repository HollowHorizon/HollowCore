package ru.hollowhorizon.hc.client.utils.tools;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class HollowItemColors {
    public static final ThreadLocal<ItemStack> targetStack = new ThreadLocal<>();

    public static void setTargetStack(ItemStack stack) {
        targetStack.set(stack);
    }

    public static int changeColor() {
        ItemStack target = targetStack.get();

        if (target != null) {
            if (target.getOrCreateTag().contains("use_glint")) {
                return target.getOrCreateTag().getInt("use_glint");
            }
        }

        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.glintColor.get(color) : RenderType.glint();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getEntityGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.entityGlintColor.get(color) : RenderType.entityGlint();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getGlintDirect() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.glintDirectColor.get(color) : RenderType.glintDirect();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getEntityGlintDirect() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.entityGlintDirectColor.get(color) : RenderType.entityGlintDirect();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getArmorGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.armorGlintColor.get(color) : RenderType.armorGlint();
    }

    @OnlyIn(Dist.CLIENT)
    public static RenderType getArmorEntityGlint() {
        int color = changeColor();
        return color >= 0 && color <= 16 ? GlintRenderType.armorEntityGlintColor.get(color) : RenderType.armorEntityGlint();
    }
}
