package ru.hollowhorizon.hc.client.particles;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;

public class HollowParticle<T> {
    private final TextureManager textureManager;
    private final ResourceLocation texture;
    private final ParticleType<T> type;
    private final int halfSize;
    private final int speed;
    private Vector3f pos = new Vector3f(0, 0, 0);
    private int lifeTime;

    public HollowParticle(ResourceLocation texture, ParticleType<T> type, int size, int speed, int lifeTime, T target) {
        this.texture = texture;
        this.type = type;
        this.halfSize = size / 2;
        this.speed = speed;
        this.lifeTime = lifeTime;
        textureManager = Minecraft.getInstance().textureManager;
    }

    public void tick() {
        this.pos.add(new Vector3f(0.1F, 0.1F, 0.1F));
        this.lifeTime -= 1;
    }

    public void render(MatrixStack stack) {
        textureManager.bind(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder builder = tessellator.getBuilder();
        builder.begin(7, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(stack.last().pose(), pos.x() - halfSize, pos.y() - halfSize, pos.z()).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0, 0).endVertex();
        builder.vertex(stack.last().pose(), pos.x() + halfSize, pos.y() - halfSize, pos.z()).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0, 0).endVertex();
        builder.vertex(stack.last().pose(), pos.x() + halfSize, pos.y() + halfSize, pos.z()).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0, 0).endVertex();
        builder.vertex(stack.last().pose(), pos.x() - halfSize, pos.y() + halfSize, pos.z()).color(1.0F, 1.0F, 1.0F, 1.0F).uv(0, 0).endVertex();
        tessellator.end();
    }
}
