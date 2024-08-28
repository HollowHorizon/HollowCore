package ru.hollowhorizon.hc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(targets = "com.mojang.math.Matrix3f")
public class Matrix3fConverter {
    //? if <=1.19.2 {
    /*@Shadow
    protected float m00;
    @Shadow
    protected float m01;
    @Shadow
    protected float m02;
    @Shadow
    protected float m10;
    @Shadow
    protected float m11;
    @Shadow
    protected float m12;
    @Shadow
    protected float m20;
    @Shadow
    protected float m21;
    @Shadow
    protected float m22;

    @Unique
    public org.joml.Matrix3f hollowcore$toJoml() {
        return new org.joml.Matrix3f()
                .m00(this.m00).m01(this.m10).m02(this.m20)
                .m10(this.m01).m11(this.m11).m12(this.m21)
                .m20(this.m02).m21(this.m12).m22(this.m22);
    }

    @Unique
    public void hollowcore$fromJoml(org.joml.Matrix3f m) {
        m00 = m.m00(); m01 = m.m10(); m02 = m.m20();
        m10 = m.m01(); m11 = m.m11(); m12 = m.m21();
        m20 = m.m02(); m21 = m.m12(); m22 = m.m22();
    }
    *///?}
}
