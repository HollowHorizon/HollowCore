package ru.hollowhorizon.hc.client.model.dae.loader.data;

/**
 * Contains the extracted data for an animated model, which includes the mesh data, and skeleton (joints heirarchy) data.
 * @author Karl
 *
 */
public class AnimatedModelData {

	private final SkeletonData joints;
	private final MeshData mesh;
	
	public AnimatedModelData(final MeshData mesh, final SkeletonData joints){
		this.joints = joints;
		this.mesh = mesh;
	}
	
	public SkeletonData getJointsData(){
		return joints;
	}
	
	public MeshData getMeshData(){
		return mesh;
	}
	
}
