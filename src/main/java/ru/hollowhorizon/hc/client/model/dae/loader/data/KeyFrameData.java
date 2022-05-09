package ru.hollowhorizon.hc.client.model.dae.loader.data;

import java.util.ArrayList;
import java.util.List;

public class KeyFrameData {

    private final float time;
    private final List<JointTransformData> jointTransforms = new ArrayList<JointTransformData>();
	
	public KeyFrameData(final float time){
		this.time = time;
	}
	
	public void addJointTransform(final JointTransformData transform){
		jointTransforms.add(transform);
	}

    public float getTime()
    {
        return time;
    }

    public List<JointTransformData> getJointTransforms()
    {
        return jointTransforms;
    }
}
