package ru.hollowhorizon.hc.client.model.dae.loader.animation.collada;

import net.minecraft.util.math.vector.Vector3f;
import org.lwjgl.BufferUtils;
import ru.hollowhorizon.hc.client.model.dae.loader.data.AnimationData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.JointTransformData;
import ru.hollowhorizon.hc.client.model.dae.loader.data.KeyFrameData;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;
import ru.hollowhorizon.hc.client.utils.tools.XmlNode;

import java.nio.FloatBuffer;
import java.util.List;

public class ColladaAnimationExtractor
{
	
	private static final Matrix4f CORRECTION = new Matrix4f().rotate((float) Math.toRadians(-90), new Vector3f(1, 0, 0));
	
	private XmlNode animationData;
	private XmlNode jointHierarchy;
	
	public ColladaAnimationExtractor(final XmlNode animationData, final XmlNode jointHierarchy){
		this.animationData = animationData;
		this.jointHierarchy = jointHierarchy;
	}
	
	public AnimationData extractAnimation(){
		final String rootNode = findRootJointName();
		final float[] times = getKeyTimes();
		final float duration = times[times.length-1];
		final KeyFrameData[] keyFrames = initKeyFrames(times);
		final List<XmlNode> animationNodes = animationData.getChildren("animation");

		for(final XmlNode jointNode : animationNodes){
			loadJointTransforms(keyFrames, jointNode, rootNode);
		}
		return new AnimationData(duration, keyFrames);
	}
	
	private float[] getKeyTimes(){
		final XmlNode timeData = animationData.getChild("animation").getChild("source").getChild("float_array");
		final String[] rawTimes = timeData.getData().split(" ");
		final float[] times = new float[rawTimes.length];
		for(int i=0;i<times.length;i++){
			times[i] = Float.parseFloat(rawTimes[i]);
		}
		return times;
	}
	
	private KeyFrameData[] initKeyFrames(final float[] times){
		final KeyFrameData[] frames = new KeyFrameData[times.length];
		for(int i=0;i<frames.length;i++){
			frames[i] = new KeyFrameData(times[i]);
		}
		return frames;
	}
	
	private void loadJointTransforms(final KeyFrameData[] frames, final XmlNode jointData, final String rootNodeId){
		final String jointNameId = getJointName(jointData); //Armature_Bone | Torso
		final String dataId = getDataId(jointData); //Armature_ArmatureAction_Bone_pose_matrix-output | Armature_Torso_pose_matrix-output
		final XmlNode transformData = jointData.getChildWithAttribute("source", "id", dataId);
		final String[] rawData = transformData.getChild("float_array").getData().split(" ");
		processTransforms(jointNameId, rawData, frames, jointNameId.equals(rootNodeId));
	}
	
	private String getDataId(final XmlNode jointData){
		final XmlNode node = jointData.getChild("sampler").getChildWithAttribute("input", "semantic", "OUTPUT");
		return node.getAttribute("source").substring(1);
	}
	
	private String getJointName(final XmlNode jointData){
		final XmlNode channelNode = jointData.getChild("channel");
		final String data = channelNode.getAttribute("target");
		return data.split("/")[0];
	}
	
	private void processTransforms(final String jointName, final String[] rawData, final KeyFrameData[] keyFrames, final boolean root){
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
		final float[] matrixData = new float[16];
		for(int i=0;i<keyFrames.length;i++){
			for(int j=0;j<16;j++){
				matrixData[j] = Float.parseFloat(rawData[i*16 + j]);
			}
			buffer.clear();
			buffer.put(matrixData);
			buffer.flip();
			final Matrix4f transform = new Matrix4f();
			transform.load(buffer);
			transform.transpose();
			if(root){
				//because up axis in Blender is different to up axis in game
				Matrix4f.mul(CORRECTION, transform, transform);
			}
			keyFrames[i].addJointTransform(new JointTransformData(jointName, transform));
		}
	}
	
	private String findRootJointName(){
		final XmlNode skeleton = jointHierarchy.getChild("visual_scene").getChildWithAttribute("node", "id", "Armature");
		return skeleton.getChild("node").getAttribute("id");
	}


}
