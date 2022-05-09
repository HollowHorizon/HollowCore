package ru.hollowhorizon.hc.client.model.dae.loader.data;

import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

/**
 * This contains the data for a transformation of one joint, at a certain time
 * in an animation. It has the name of the joint that it refers to, and the
 * local transform of the joint in the pose position.
 * 
 * @author Karl
 *
 */
public class JointTransformData {

    private final String   jointNameId;
    private final Matrix4f jointLocalTransform;

	public JointTransformData(final String jointNameId, final Matrix4f jointLocalTransform) {
		this.jointNameId = jointNameId;
		this.jointLocalTransform = jointLocalTransform;
	}

    public String getJointNameId()
    {
        return jointNameId;
    }

    public Matrix4f getJointLocalTransform()
    {
        return jointLocalTransform;
    }
}
