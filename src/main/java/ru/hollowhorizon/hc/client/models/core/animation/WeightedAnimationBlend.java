package ru.hollowhorizon.hc.client.models.core.animation;

import ru.hollowhorizon.hc.client.utils.math.Matrix4d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WeightedAnimationBlend implements IPoseProvider {

    private final List<AnimationWeight> animBlends;
    private IPose basePose;
    private final Pose workFrame;


    public WeightedAnimationBlend(){
        workFrame = new Pose();
        animBlends = new ArrayList<>();
    }

    public IPose getPose(){
        if (basePose != null){
            bake();
        }
        return workFrame;
    }

    public void setBlends(IPose basePose, AnimationWeight... blends){
        workFrame.setJointCount(basePose.getJointCount());
        this.basePose = basePose;
        animBlends.clear();
        animBlends.addAll(Arrays.asList(blends));
    }

    public void simpleBlend(IPose basePose, IPose otherPose, float time){
        setBlends(basePose, new AnimationWeight(otherPose, time));
    }

    private void bake(){
        for (int i = 0; i < basePose.getJointCount(); i++){
            workFrame.setJointMatrix(i, basePose.getJointMatrix(i));
            Matrix4d newJoint = workFrame.getJointMatrix(i);
            for (AnimationWeight blend : animBlends){
                newJoint.lerp(blend.provider.getJointMatrix(i), blend.weight);
            }
        }
    }
}
