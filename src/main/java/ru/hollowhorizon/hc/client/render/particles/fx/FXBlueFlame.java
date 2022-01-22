package ru.hollowhorizon.hc.client.render.particles.fx;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.math.vector.Vector3d;
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;

import javax.annotation.Nullable;

public class FXBlueFlame extends HollowParticleBase {
    private final IAnimatedSprite sprite;
    private static final IParticleRenderType NORMAL_RENDER = new FXBlueFlameType();

    public FXBlueFlame(ClientWorld world, double x, double y, double z, IAnimatedSprite sprite) {
        super(world, x, y, z, 0, 0, 0);
        this.sprite = sprite;
        this.yd *= 0.20000000298023224D;
        this.xd = 0.0D;
        this.zd = 0.0D;
        this.setColor(1.0F, 0.0F, 1.0F);
        this.quadSize *= 0.45F;
        this.lifetime = 20;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprite);

    }

    private void setMoveVelocity(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getQuadSize(float partialTicks) {
        return this.quadSize * (float)(this.lifetime - this.age + 1) / (float)this.lifetime;
    }

    public Vector3d getPosition() {
        return new Vector3d(this.x, this.y, this.z);
    }

    protected int getLightColor(float partialTick) {
        return 15728640;
    }


    @Override
    public IParticleRenderType getRenderType() {
        return NORMAL_RENDER;
    }

    public static final class FXBlueFlameType implements IParticleRenderType {
        public void begin(BufferBuilder bufferBuilder, TextureManager textureManager) {
            FXBlueFlame.beginRenderCommon(bufferBuilder, textureManager);
        }

        public void end(Tessellator tessellator) {
            tessellator.end();
            FXBlueFlame.endRenderCommon();
        }

        public String toString() {
            return "hc:blue_flame";
        }
    }

    public static class Factory extends HollowParticleBase.Factory {
        public Factory(IAnimatedSprite spriteSet) {
            super(spriteSet);
        }

        @Nullable
        @Override
        public Particle createParticle(HollowParticleType type, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            FXBlueFlame particle = new FXBlueFlame(worldIn, x, y, z, this.spriteSet);
            particle.move(xSpeed, ySpeed, zSpeed);

            particle.setColor(1.0F, 1.0F, 1.0F);
            this.configureParticle(particle, type);
            return particle;
        }
    }

}
