package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract Framebuffer getMainRenderTarget();

}
