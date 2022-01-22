package ru.hollowhorizon.hc.client.render.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import ru.hollowhorizon.hc.client.render.particles.params.*;
import ru.hollowhorizon.hc.common.registry.ModParticles;

import javax.annotation.Nullable;

public class HollowParticleType extends ParticleType<HollowParticleType> implements IParticleData {
    private ParticleType<HollowParticleType> type;
    private ParticleColor color;
    private ParticleFloat scale;
    private ParticleInt life;
    private ParticleInt lifePadding;
    private ParticleFloat gravity;
    private ParticleBoolean physics;
    private IParticleMoveType mover;
    private ParticleItemStack stack;
    public boolean disableDepthTest;
    public static final Codec<HollowParticleType> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.FLOAT.fieldOf("r").forGetter(d -> (d.color != null) ? d.color.getRed() : 1.0f), Codec.FLOAT.fieldOf("g").forGetter(d -> (d.color != null) ? d.color.getGreen() : 1.0f), Codec.FLOAT.fieldOf("b").forGetter(d -> (d.color != null) ? d.color.getBlue() : 1.0f), Codec.FLOAT.fieldOf("scale").forGetter(d -> (d.scale != null) ? d.scale.value() : 1.0f), Codec.INT.fieldOf("life").forGetter(d -> (d.life != null) ? d.life.value() : 20), Codec.INT.fieldOf("lifePadding").forGetter(d -> (d.lifePadding != null) ? d.lifePadding.value() : 0), Codec.FLOAT.fieldOf("gravity").forGetter(d -> (d.gravity != null) ? d.gravity.value() : 0.0f), Codec.BOOL.fieldOf("physics").forGetter(d -> d.physics != null && d.physics.value()), Codec.BOOL.fieldOf("disableDepthTest").forGetter(d -> d.disableDepthTest)).apply(instance, HollowParticleType::new));;
    public static final IParticleData.IDeserializer<HollowParticleType> DESERIALIZER = new Deserializer();

    public HollowParticleType() {
        super(false, HollowParticleType.DESERIALIZER);
    }

    public HollowParticleType(final ParticleType<HollowParticleType> type) {
        this();
        this.type = type;
    }

    private HollowParticleType(final float r, final float g, final float b, final float scale, final int life, final int lifePadding, final float gravity, final boolean physics, final boolean disableDepth) {
        super(disableDepth, HollowParticleType.DESERIALIZER);
        this.setColor(r, g, b);
        this.setScale(scale);
        this.setGravity(gravity);
        this.setPhysics(physics);
        this.setMaxAge(life);
    }

    private HollowParticleType(final ParticleType<HollowParticleType> type, final ParticleColor color, final ParticleFloat scale, final ParticleInt life, final ParticleInt lifePadding, final ParticleFloat gravity, final ParticleBoolean physics, final ParticleItemStack stack, final IParticleMoveType mover, final boolean disableDepth) {
        super(disableDepth, HollowParticleType.DESERIALIZER);
        this.color = color;
        this.scale = scale;
        this.life = life;
        this.lifePadding = lifePadding;
        this.gravity = gravity;
        this.physics = physics;
        this.mover = mover;
    }

    public Codec<HollowParticleType> codec() {
        return HollowParticleType.CODEC;
    }

    public void writeToNetwork(final PacketBuffer packetBuffer) {
        if (this.color != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeUtf(this.color.serialize());
        }
        else {
            packetBuffer.writeBoolean(false);
        }
        ParticleFloat.serialize(this.scale, packetBuffer);
        ParticleInt.serialize(this.life, packetBuffer);
        ParticleInt.serialize(this.lifePadding, packetBuffer);
        ParticleFloat.serialize(this.gravity, packetBuffer);
        ParticleBoolean.serialize(this.physics, packetBuffer);
        if (this.mover != null) {
            packetBuffer.writeBoolean(true);
            this.mover.serialize(packetBuffer);
        }
        else {
            packetBuffer.writeBoolean(false);
        }
        packetBuffer.writeBoolean(this.disableDepthTest);
    }

    public String writeToString() {
        return this.getType().getRegistryName().toString() + " " + ((this.color != null) ? this.color.serialize() : "NO COLOR OVERRIDE") + " " + ((this.scale != null) ? this.scale.serialize() : "NO SCALE OVERRIDE") + ((this.life != null) ? this.life.serialize() : "NO LIFE OVERRIDE") + ((this.lifePadding != null) ? this.lifePadding.serialize() : "NO LIFE PADDING") + ((this.mover != null) ? this.mover.serialize() : "NO MOVE OVERRIDE");
    }

    public HollowParticleType setColor(final float r, final float g, final float b) {
        this.color = new ParticleColor(r, g, b);
        return this;
    }

    public HollowParticleType setScale(final float scale) {
        this.scale = new ParticleFloat(scale);
        return this;
    }

    public HollowParticleType setColor(final int r, final int g, final int b) {
        this.color = new ParticleColor(r, g, b);
        return this;
    }

    public HollowParticleType setMaxAge(final int age) {
        this.life = new ParticleInt(age);
        return this;
    }

    public HollowParticleType setAgePadding(final int padding) {
        this.lifePadding = new ParticleInt(padding);
        return this;
    }

    public HollowParticleType setGravity(final float gravity) {
        this.gravity = new ParticleFloat(gravity);
        return this;
    }

    public HollowParticleType setPhysics(final boolean physics) {
        this.physics = new ParticleBoolean(physics);
        return this;
    }

    public HollowParticleType setStack(final ItemStack stack) {
        this.stack = new ParticleItemStack(stack);
        return this;
    }

    public HollowParticleType setMover(final IParticleMoveType mover) {
        this.mover = mover;
        return this;
    }

    @Nullable
    public IParticleMoveType getMover() {
        return this.mover;
    }

    @Nullable
    public ParticleColor getColor() {
        return this.color;
    }

    @Nullable
    public ParticleFloat getScale() {
        return this.scale;
    }

    @Nullable
    public ParticleInt getLife() {
        return this.life;
    }

    @Nullable
    public ParticleInt getLifePadding() {
        return this.lifePadding;
    }

    @Nullable
    public ParticleFloat getGravity() {
        return this.gravity;
    }

    @Nullable
    public ParticleBoolean getPhysics() {
        return this.physics;
    }

    @Nullable
    public ParticleItemStack getStack() {
        return this.stack;
    }

    public ParticleType<HollowParticleType> getType() {
        return this.type == null ? ModParticles.BLUE_FLAME : this.type;
    }



    static final class Deserializer implements IParticleData.IDeserializer<HollowParticleType> {
        public HollowParticleType fromCommand(final ParticleType<HollowParticleType> type, final StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            String colorString = null;
            String scaleString = null;
            String lifeString = null;
            String lifePaddingString = null;
            String gravityString = null;
            String physString = null;
            String stackString = null;
            try { colorString = reader.readString(); }
            catch (Exception ignored) {}
            try { scaleString = reader.readString(); }
            catch (Exception ignored) {}
            try { lifeString = reader.readString(); }
            catch (Exception ignored) {}
            try { lifePaddingString = reader.readString(); }
            catch (Exception ignored) {}
            try { gravityString = reader.readString(); }
            catch (Exception ignored) {}
            try { physString = reader.readString(); }
            catch (Exception ignored) {}
            try { stackString = reader.readString(); }
            catch (Exception ignored) {}
            return new HollowParticleType(type, ParticleColor.deserialize(colorString), ParticleFloat.deserialize(scaleString), ParticleInt.deserialize(lifeString), ParticleInt.deserialize(lifePaddingString), ParticleFloat.deserialize(gravityString), ParticleBoolean.deserialize(physString), ParticleItemStack.deserialize(stackString), null, reader.readBoolean());
        }

        public HollowParticleType fromNetwork(final ParticleType<HollowParticleType> type, final PacketBuffer buffer) {
            ParticleColor color = null;
            if (buffer.readBoolean()) {
                color = ParticleColor.deserialize(buffer.readUtf(32767));
            }
            return new HollowParticleType(type, color, ParticleFloat.deserialize(buffer), ParticleInt.deserialize(buffer), ParticleInt.deserialize(buffer), ParticleFloat.deserialize(buffer), ParticleBoolean.deserialize(buffer), ParticleItemStack.deserialize(buffer), IParticleMoveType.fromID(buffer.readInt()).deserialize(buffer), buffer.readBoolean());
        }
    }

}
