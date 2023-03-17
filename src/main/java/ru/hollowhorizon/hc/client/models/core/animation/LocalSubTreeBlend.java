package ru.hollowhorizon.hc.client.models.core.animation;

import java.util.List;

import ru.hollowhorizon.hc.client.utils.math.Matrix4d;

public class LocalSubTreeBlend implements IPoseProvider {

    private InterpolationFramesReturn frames;
    private final Pose workFrame;
    private final List<Integer> boneIds;


    public LocalSubTreeBlend(List<Integer> boneIds){
        workFrame = new Pose();
        this.boneIds = boneIds;

    }

    public void setFrames(InterpolationFramesReturn ret){
        frames = ret;
        workFrame.setJointCount(frames.current.getJointCount());
    }

    @Override
    public IPose getPose() {
        if (frames != null){
            bake();
        }
        return workFrame;
    }

    private void bake(){
        for (int i : boneIds){
            workFrame.setJointMatrix(i, frames.current.getLocalJointMatrix(i));
            Matrix4d newJoint = workFrame.getJointMatrix(i);
            newJoint.lerp(frames.next.getLocalJointMatrix(i), frames.partialTick);
        }
    }
}
