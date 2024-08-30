package ru.hollowhorizon.hc.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
//? if <=1.19.2 {
/*import ru.hollowhorizon.hc.api.Matrix4fAccessor;
*///?}

@Mixin(targets = "com/mojang/math/Matrix4f")
public class Matrix4fMixin
        //? if <=1.19.2
        /*implements Matrix4fAccessor*/
{
    //? if <=1.19.2 {
    /*@Shadow
    protected float m00;
    @Shadow
    protected float m01;
    @Shadow
    protected float m02;
    @Shadow
    protected float m03;
    @Shadow
    protected float m10;
    @Shadow
    protected float m11;
    @Shadow
    protected float m12;
    @Shadow
    protected float m13;
    @Shadow
    protected float m20;
    @Shadow
    protected float m21;
    @Shadow
    protected float m22;
    @Shadow
    protected float m23;
    @Shadow
    protected float m30;
    @Shadow
    protected float m31;
    @Shadow
    protected float m32;
    @Shadow
    protected float m33;


    @Unique
    @Override
    public org.joml.Matrix4f hollowcore$toJoml() {
        return new org.joml.Matrix4f()
                .m00(this.m00).m01(this.m10).m02(this.m20).m03(this.m30)
                .m10(this.m01).m11(this.m11).m12(this.m21).m13(this.m31)
                .m20(this.m02).m21(this.m12).m22(this.m22).m23(this.m32)
                .m30(this.m03).m31(this.m13).m32(this.m23).m33(this.m33);
    }

    @Unique
    @Override
    public void hollowcore$fromJoml(org.joml.Matrix4f m) {
        m00 = m.m00();
        m01 = m.m10();
        m02 = m.m20();
        m03 = m.m30();
        m10 = m.m01();
        m11 = m.m11();
        m12 = m.m21();
        m13 = m.m31();
        m20 = m.m02();
        m21 = m.m12();
        m22 = m.m22();
        m23 = m.m32();
        m30 = m.m03();
        m31 = m.m13();
        m32 = m.m23();
        m33 = m.m33();
    }
    *///?}
}
