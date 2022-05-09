package ru.hollowhorizon.hc.client.render.shader;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class SwirlTest extends PostProcessingEffect<SwirlTest> {
    private int angle = 0;
    private int angleUniformID = -1;

    public SwirlTest setAngle(int angle) {
        this.angle = angle;
        return this;
    }

    @Override
    protected ResourceLocation[] getShaders() {
        return new ResourceLocation[] {
                new ResourceLocation("hc:shaders/postprocessing/swirl/swirl.vsh"),
                new ResourceLocation("hc:shaders/postprocessing/swirl/swirl.fsh")
        };
    }

    @Override
    protected boolean initEffect() {
        this.angleUniformID = GL20.glGetUniformLocation(this.getShaderProgram(), "time");
        return /*this.timeUniformID >= 0 && this.scaleUniformID >= 0 && this.timeScaleUniformID >= 0 && this.multiplierUniformID >= 0 && this.xOffsetUniformID >= 0 && this.yOffsetUniformID >= 0*/true;
    }

    @Override
    protected void uploadUniforms(float partialTicks) {
        this.uploadInt(this.angleUniformID, this.angle);
    }
}
