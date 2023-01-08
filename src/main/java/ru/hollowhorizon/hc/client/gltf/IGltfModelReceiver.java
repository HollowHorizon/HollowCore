package ru.hollowhorizon.hc.client.gltf;

import de.javagl.jgltf.model.GltfModel;
import net.minecraft.util.ResourceLocation;

import java.util.List;

public interface IGltfModelReceiver {

	ResourceLocation getModelLocation();
	
	default void onReceiveSharedModel(RenderedGltfModel renderedModel) {}
	
	default boolean isReceiveSharedModel(GltfModel gltfModel, List<GltfRenderData> gltfRenderDatas) {
		return true;
	}
}
