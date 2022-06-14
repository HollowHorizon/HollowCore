package ru.hollowhorizon.hc.client.model.dae.loader.model.collada;

import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.BufferUtils;
import ru.hollowhorizon.hc.client.model.dae.loader.data.JointData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.SkeletonData;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;
import ru.hollowhorizon.hc.client.utils.tools.XmlNode;

import java.nio.FloatBuffer;
import java.util.List;

public class ColladaSkeletonLoader
{

	private XmlNode armatureData;
	
	private List<String> boneOrder;
	
	private int jointCount = 0;
	
	private static final Matrix4f CORRECTION = new Matrix4f().rotate((float) Math.toRadians(-90), new Vector3f(1, 0, 0));

	public ColladaSkeletonLoader(final XmlNode visualSceneNode, final List<String> boneOrder) {
		this.armatureData = visualSceneNode.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
		this.boneOrder = boneOrder;
	}
	
	public SkeletonData extractBoneData(){
		final XmlNode headNode = armatureData.getChild("node");
		final JointData headJoint = loadJointData(headNode, true);
		return new SkeletonData(jointCount, headJoint);
	}
	
	private JointData loadJointData(final XmlNode jointNode, final boolean isRoot){
		final JointData joint = extractMainJointData(jointNode, isRoot);
		for(final XmlNode childNode : jointNode.getChildren("node")){
			joint.addChild(loadJointData(childNode, false));
		}
		return joint;
	}
	
	private JointData extractMainJointData(final XmlNode jointNode, final boolean isRoot){
		final String nameId = jointNode.getAttribute("id");
		int index = boneOrder.indexOf(nameId);
		if(index == -1) index = boneOrder.indexOf(nameId.substring(9));

		final String[] matrixData = jointNode.getChild("matrix").getData().split(" ");
		final Matrix4f matrix = new Matrix4f();
		matrix.load(convertData(matrixData));
		matrix.transpose();
		if(isRoot){
			//because in Blender z is up, but in our game y is up.
			Matrix4f.mul(CORRECTION, matrix, matrix);
		}
		jointCount++;
		return new JointData(index, nameId, matrix);
	}
	
	private FloatBuffer convertData(final String[] rawData){
		final float[] matrixData = new float[16];
		for(int i=0;i<matrixData.length;i++){
			matrixData[i] = Float.parseFloat(rawData[i]);
		}
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		buffer.put(matrixData);
		buffer.flip();
		return buffer;
	}

}
