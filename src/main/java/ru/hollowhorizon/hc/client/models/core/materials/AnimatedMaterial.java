package ru.hollowhorizon.hc.client.models.core.materials;


import net.minecraft.client.shader.ShaderLoader;
import ru.hollowhorizon.hc.client.models.core.animation.AnimationFrame;
import ru.hollowhorizon.hc.client.models.core.animation.IPose;

public class AnimatedMaterial extends BTMaterial {

    public MaterialUniform jointsUniform;
    public MaterialUniform inverseBindPoseUniform;

    public AnimatedMaterial(int program, ShaderLoader vert, ShaderLoader frag) {
        super(program, vert, frag);
        jointsUniform = new MaterialUniform("joints_matrix",
                MaterialUniform.UniformType.vecmat4x4, AnimationFrame.MAX_JOINTS, this);
        uniforms.add(jointsUniform);
        inverseBindPoseUniform = new MaterialUniform("inverse_bind_pose",
                MaterialUniform.UniformType.vecmat4x4, AnimationFrame.MAX_JOINTS, this);
        uniforms.add(inverseBindPoseUniform);
    }

    @Override
    public void uploadInverseBindPose(IPose pose){
        inverseBindPoseUniform.set(pose.getJointCount(), pose.getJointMatrices());
        inverseBindPoseUniform.upload();
    }

    @Override
    public void uploadAnimationFrame(IPose pose) {
        jointsUniform.set(pose.getJointCount(), pose.getJointMatrices());
        jointsUniform.upload();
    }
}
