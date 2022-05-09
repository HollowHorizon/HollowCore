package ru.hollowhorizon.hc.client.model.dae.loader.data;

public class SkeletonData {

    private final int       jointCount;
    private final JointData headJoint;
	
	public SkeletonData(final int jointCount, final JointData headJoint){
		this.jointCount = jointCount;
		this.headJoint = headJoint;
	}

    public int getJointCount()
    {
        return jointCount;
    }

    public JointData getHeadJoint()
    {
        return headJoint;
    }
}
