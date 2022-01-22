package ru.hollowhorizon.hc.client.render.particles.base;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType;
import ru.hollowhorizon.hc.client.render.particles.moves.FXMovementType;

import javax.annotation.Nonnull;
import java.util.ArrayList;

public abstract class HollowParticleBase extends SpriteTexturedParticle {
    public static final IParticleRenderType NORMAL_RENDER;

    static {
        NORMAL_RENDER = new NormalRender();
    }

    protected FXMovementType movementType;
    protected float maxAlpha;
    protected float life_padding;
    protected ArrayList<Vector3d> colorTransitions;
    private Vector3d start;
    private Vector3d end;
    private Vector3d control_a;
    private Vector3d control_b;
    private ItemStack stack;
    private float uo;
    private float vo;
    private boolean decay_velocity;
    private boolean orbit_clockwise;
    private double angle;

    protected HollowParticleBase(final ClientWorld world, final double x, final double y, final double z, final double xSpeed, final double ySpeed, final double zSpeed) {
        super(world, x, y, z, xSpeed, ySpeed, zSpeed);
        this.stack = null;
        this.decay_velocity = false;
        this.orbit_clockwise = true;
        this.maxAlpha = 1.0f;
        this.life_padding = 0.0f;
        this.colorTransitions = new ArrayList<Vector3d>();
    }

    protected static void beginRenderCommon(final BufferBuilder buffer, final TextureManager textureManager) {
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 1);
        RenderSystem.alphaFunc(516, 0.003921569f);
        RenderSystem.disableLighting();
        textureManager.bind(AtlasTexture.LOCATION_PARTICLES);
        final Texture tex = textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES);
        tex.setBlurMipmap(true, false);
        buffer.begin(7, DefaultVertexFormats.PARTICLE);
    }

    protected static void endRenderCommon() {
        Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_PARTICLES).restoreLastBlurMipmap();
        RenderSystem.alphaFunc(516, 0.1f);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (!this.apply_aging()) {
            this.move();
        }
    }

    public void setMoveVelocity(final double x, final double y, final double z, final boolean decay) {
        this.xd = x;
        this.yd = y;
        this.zd = z;
        this.decay_velocity = decay;
        this.movementType = FXMovementType.VELOCITY;
    }

    public void setMoveStationary() {
        this.xd = 0.0;
        this.yd = 0.0;
        this.zd = 0.0;
        this.movementType = FXMovementType.STATIONARY;
    }

    public void setMoveLerp(final Vector3d start, final Vector3d end) {
        this.start = start;
        this.end = end;
        this.movementType = FXMovementType.LERP_POINT;
    }

    public void setMoveLerp(final double x, final double y, final double z, final double goal_x, final double goal_y, final double goal_z) {
        this.setMoveLerp(new Vector3d(x, y, z), new Vector3d(goal_x, goal_y, goal_z));
    }

    public void setMoveBezier(final Vector3d start, final Vector3d end, final Vector3d controlA, final Vector3d controlB) {
        this.start = start;
        this.end = end;
        this.control_a = controlA;
        this.control_b = controlB;
        this.movementType = FXMovementType.BEZIER_POINT;
    }

    public void setMoveBezier(final Vector3d start, final Vector3d end) {
        this.start = start;
        this.end = end;
        this.generateBezierControlPoints();
        this.movementType = FXMovementType.BEZIER_POINT;
    }

    public void setMoveBezier(final double x, final double y, final double z, final double goal_x, final double goal_y, final double goal_z) {
        this.setMoveBezier(new Vector3d(x, y, z), new Vector3d(goal_x, goal_y, goal_z));
    }

    public void setMoveRandomly(final double x, final double y, final double z) {
        this.movementType = FXMovementType.VELOCITY;
        this.xd = -x + Math.random() * 2.0 * y;
        this.yd = -y + Math.random() * 2.0 * y;
        this.zd = -z + Math.random() * 2.0 * z;
    }

    public void setMoveOrbit(final double cX, final double cY, final double cZ, final double forwardSpeed, final double upSpeed, final double radius) {
        this.start = new Vector3d(cX, cY, cZ);
        this.end = new Vector3d(forwardSpeed, radius, upSpeed);
        this.yd = upSpeed;
        this.orbit_clockwise = (this.level.random.nextInt(10) < 5);
        this.lifetime = 30;
        this.angle = Math.random() * 360.0;
        this.movementType = FXMovementType.ORBIT;
        this.moveOrbit();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void setMoveSphereOrbit(final double cX, final double cY, final double cZ, final double forwardSpeed, final double tilt, final double radius) {
        this.start = new Vector3d(cX, cY, cZ);
        this.end = new Vector3d(forwardSpeed, radius, tilt);
        this.lifetime = 30;
        this.orbit_clockwise = (this.level.random.nextInt(10) < 5);
        this.angle = Math.random() * 360.0;
        this.movementType = FXMovementType.SPHERE_ORBIT;
        this.moveSphereOrbit();
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
    }

    public void setRenderAsStackParticles(final ItemStack stack) {
        this.stack = stack.copy();
        this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(this.stack, this.level, null).getParticleIcon());
        this.uo = this.random.nextFloat() * 3.0f;
        this.vo = this.random.nextFloat() * 3.0f;
        this.quadSize /= 2.0f;
    }

    protected float getU0() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getU((this.uo + 1.0f) / 4.0f * 16.0f);
        }
        return super.getU0();
    }

    protected float getU1() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getU(this.uo / 4.0f * 16.0f);
        }
        return super.getU1();
    }

    protected float getV0() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getV((double) (this.vo / 4.0f * 16.0f));
        }
        return super.getV0();
    }

    protected float getV1() {
        if (this.stack != null && !this.stack.isEmpty()) {
            return this.sprite.getV((this.vo + 1.0f) / 4.0f * 16.0f);
        }
        return super.getV1();
    }

    private void generateBezierControlPoints() {
        final Vector3d o1 = new Vector3d(this.start.x, this.start.y, this.start.z);
        Vector3d midPoint = new Vector3d((this.start.x + this.end.x) / 2.0f,
                (this.start.y + this.end.y) / 2.0f,
                (this.start.z + this.end.z) / 2.0f);
        midPoint = new Vector3d(midPoint.x - o1.x, midPoint.y - o1.y, midPoint.y - o1.y);

        float yaw = 1.5707964f;
        final float f = MathHelper.cos(yaw);
        final float f2 = MathHelper.cos(yaw);
        final double d0 = midPoint.x * (double) f + midPoint.z * (double) f2;
        final double d2 = midPoint.y;
        final double d3 = midPoint.z * (double) f - midPoint.x * (double) f2;
        midPoint = new Vector3d(d0, d2, d3);


        this.control_a = new Vector3d((double) (this.start.x + (this.end.x - this.start.x) / 3.0f), (double) (this.start.y + (this.end.y - this.start.y) / 3.0f), (double) (this.start.z + (this.end.z - this.start.z) / 3.0f));
        this.control_b = new Vector3d((double) (this.start.x + (this.end.x - this.start.x) / 3.0f * 2.0f), (double) (this.start.y + (this.end.y - this.start.y) / 3.0f * 2.0f), (double) (this.start.z + (this.end.z - this.start.z) / 3.0f * 2.0f));
        this.control_a = this.control_a.add(midPoint);
        this.control_b = this.control_b.add(midPoint);
    }

    protected void move() {
        if (this.movementType != null) {
            switch (MovementType.HollowFXMovementType[this.movementType.ordinal()]) {
                case 1: {
                    this.moveBezier();
                    break;
                }
                case 2: {
                    this.moveLerp();
                    break;
                }
                case 3: {
                    this.moveVelocity();
                    break;
                }
                case 4: {
                    this.moveOrbit();
                    break;
                }
                case 5: {
                    this.moveSphereOrbit();
                    break;
                }
            }
        }
    }

    protected boolean apply_aging() {
        if (this.age++ >= this.lifetime + this.life_padding) {
            this.remove();
        }

        final float agePct = Math.max(0.0F, Math.min(1.0F, this.age / (float) this.lifetime));
        ;
        this.lerpAlpha(agePct);
        this.lerpColors(agePct);
        return this.removed;
    }

    private void lerpAlpha(final float agePct) {
        float alpha_T = this.maxAlpha;
        if (agePct < 0.2f) {
            alpha_T = agePct / 0.2f * this.maxAlpha;
        } else if (agePct > 0.8f) {
            alpha_T = (1.0f - (agePct - 0.8f) / 0.2f) * this.maxAlpha;
        }
        this.setAlpha(alpha_T);
    }

    private void lerpColors(final float agePct) {
        if (this.colorTransitions == null || this.colorTransitions.size() < 2) {
            return;
        }
        final int cSize = this.colorTransitions.size();
        final int cIndex = (int) Math.floor(cSize * agePct) % cSize;
        int nIndex = cIndex + 1;
        if (nIndex > cSize - 1) {
            nIndex = cSize - 1;
        }
        final int colorAge = this.lifetime / cSize;
        final float color_T = this.age % colorAge / (float) colorAge;

        final Vector3d clr = new Vector3d(
                this.colorTransitions.get(cIndex).x + (this.colorTransitions.get(nIndex).x - this.colorTransitions.get(cIndex).x) * color_T,
                this.colorTransitions.get(cIndex).y + (this.colorTransitions.get(nIndex).y - this.colorTransitions.get(cIndex).y) * color_T,
                this.colorTransitions.get(cIndex).z + (this.colorTransitions.get(nIndex).z - this.colorTransitions.get(cIndex).z) * color_T
        );

        this.setColor((float) clr.x / 255.0f, (float) clr.y / 255.0f, (float) clr.z / 255.0f);
    }

    private void moveOrbit() {
        final Vector3d end = new Vector3d(this.end.x, this.end.y + 0.01f, this.end.z);

        if (this.orbit_clockwise) {
            this.angle += this.end.x;
        } else {
            this.angle -= this.end.x;
        }
        final double pX = this.start.x + Math.cos(this.angle) * this.end.y;
        final double pZ = this.start.z + Math.sin(this.angle) * this.end.y;
        this.move(0.0, this.yd, 0.0);
        this.setPos(pX, this.y, pZ);
    }

    private void moveSphereOrbit() {
        this.angle += this.end.x;
        final float pX = (float) Math.cos(this.angle);
        final float pZ = (float) Math.sin(this.angle);
        final Vector3f horizPos = new Vector3f(pX, 0.0f, pZ);
        final Quaternion rotation = Vector3f.XP.rotationDegrees((float) (this.end.z + this.age));
        horizPos.transform(rotation);
        horizPos.mul((float) this.end.y);
        this.setPos((double) (this.start.x + horizPos.x()), (double) (this.start.y + horizPos.y()), (double) (this.start.z + horizPos.z()));
    }

    private void moveVelocity() {
        this.move(this.xd, this.yd, this.zd);
        if (this.decay_velocity) {
            if (this.y == this.yo) {
                this.xd *= 1.1;
                this.zd *= 1.1;
            }
            this.xd *= 0.9599999785423279;
            this.yd *= 0.9599999785423279;
            this.zd *= 0.9599999785423279;
            if (this.onGround) {
                this.xd *= 0.699999988079071;
                this.zd *= 0.699999988079071;
            }
        }
        if (this.gravity > 0.0f) {
            this.yd -= this.gravity;
        }
    }

    private void moveLerp() {
        final float agePct = this.age / (float) this.lifetime;

        final Vector3d pos = new Vector3d(
                this.start.x + (this.end.x - this.start.x) * agePct,
                this.start.y + (this.end.y - this.start.y) * agePct,
                this.start.z + (this.end.z - this.start.z) * agePct
        );

        this.setPos(pos.x, pos.y, pos.z);
    }

    private void moveBezier() {
        this.setPosition(bezier(this.start, this.end, this.control_a, this.control_b, this.age / (float) this.lifetime));
    }

    private Vector3d bezier(final Vector3d start, final Vector3d end, final Vector3d control_1, final Vector3d control_2, float time) {
        if (time < 0.0f) {
            time = 0.0f;
        }
        else if (time > 1.0f) {
            time = 1.0f;
        }
        final float one_minus_t = 1.0f - time;
        Vector3d retValue = new Vector3d(0.0, 0.0, 0.0);
        final Vector3d[] terms = { start.scale(one_minus_t * one_minus_t * one_minus_t), control_1.scale(3.0f * one_minus_t * one_minus_t * time), control_2.scale(3.0f * one_minus_t * time * time), end.scale(time * time * time) };
        for (int i = 0; i < 4; ++i) {
            retValue = retValue.add(terms[i]);
        }
        return retValue;
    };

    private void setPosition(final Vector3d position) {
        this.setPos(position.x, position.y, position.z);
    }

    protected int getLightColor(final float partialTick) {
        return 15728880;
    }

    public void setLifePadding(final int padding) {
        this.life_padding = (float) padding;
    }

    @Nonnull
    public IParticleRenderType func_217558_b() {
        return (this.stack != null && !this.stack.isEmpty()) ? IParticleRenderType.TERRAIN_SHEET : HollowParticleBase.NORMAL_RENDER;
    }

    public static final class NormalRender implements IParticleRenderType {
        public void begin(final BufferBuilder bufferBuilder, final TextureManager textureManager) {
            HollowParticleBase.beginRenderCommon(bufferBuilder, textureManager);
        }

        public void end(final Tessellator tessellator) {
            tessellator.end();
            HollowParticleBase.endRenderCommon();
        }

        @Override
        public String toString() {
            return "hc:particle_renderer";
        }
    }

    public static class MovementType {
        static final int[] HollowFXMovementType = new int[FXMovementType.values().length];

        static {
            try { HollowFXMovementType[FXMovementType.BEZIER_POINT.ordinal()] = 1; }
            catch (NoSuchFieldError ignored) { }
            try { HollowFXMovementType[FXMovementType.LERP_POINT.ordinal()] = 2; }
            catch (NoSuchFieldError ignored) { }
            try { HollowFXMovementType[FXMovementType.VELOCITY.ordinal()] = 3; }
            catch (NoSuchFieldError ignored) { }
            try { HollowFXMovementType[FXMovementType.ORBIT.ordinal()] = 4; }
            catch (NoSuchFieldError ignored) { }
            try { HollowFXMovementType[FXMovementType.SPHERE_ORBIT.ordinal()] = 5; }
            catch (NoSuchFieldError ignored) { }
            try { HollowFXMovementType[FXMovementType.STATIONARY.ordinal()] = 6; }
            catch (NoSuchFieldError ignored) { }
        }

    }

    public static abstract class Factory implements IParticleFactory<HollowParticleType> {
        protected final IAnimatedSprite spriteSet;

        public Factory(IAnimatedSprite spriteSet) {
            this.spriteSet = spriteSet;
        }

        public abstract Particle createParticle(HollowParticleType var1, ClientWorld var2, double var3, double var5, double var7, double var9, double var11, double var13);

        protected void configureParticle(HollowParticleBase particle, HollowParticleType typeIn) {
            if(typeIn.getColor() != null) {
                particle.colorTransitions.clear();
                particle.setColor(typeIn.getColor().getRed(), typeIn.getColor().getGreen(), typeIn.getColor().getBlue());
            }

            if(typeIn.getScale() != null) {
                particle.scale(typeIn.getScale().value());
            }

            if(typeIn.getLife() != null) {
                particle.setLifetime(typeIn.getLife().value());
            }

            if(typeIn.getLifePadding() != null) {
                particle.setLifePadding(typeIn.getLife().value());
            }

            if(typeIn.getGravity() != null) {
                particle.gravity = typeIn.getGravity().value();
            }

            if(typeIn.getPhysics() != null) {
                particle.hasPhysics = typeIn.getPhysics().value();
            }

            if(typeIn.getMover() != null) {
                typeIn.getMover().configureParticle(particle);
            }

            if(typeIn.getStack() != null) {
                particle.setRenderAsStackParticles(typeIn.getStack().value());
            }

        }
    }

}
