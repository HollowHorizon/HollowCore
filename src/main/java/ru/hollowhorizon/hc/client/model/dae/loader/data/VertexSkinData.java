package ru.hollowhorizon.hc.client.model.dae.loader.data;

import java.util.ArrayList;
import java.util.List;

public class VertexSkinData {
	
	public final List<Integer> jointIds = new ArrayList<Integer>();
	public final List<Float> weights = new ArrayList<Float>();
	
	public void addJointEffect(final int jointId, final float weight){
		for(int i=0;i<weights.size();i++){
			if(weight > weights.get(i)){
				jointIds.add(i, jointId);
				weights.add(i, weight);
				return;
			}
		}
		jointIds.add(jointId);
		weights.add(weight);
	}
	
	public void limitJointNumber(final int max){
		if(jointIds.size() > max){
			final float[] topWeights = new float[max];
			final float total = saveTopWeights(topWeights);
			refillWeightList(topWeights, total);
			removeExcessJointIds(max);
		}else if(jointIds.size() < max){
			fillEmptyWeights(max);
		}
	}

	private void fillEmptyWeights(final int max){
		while(jointIds.size() < max){
			jointIds.add(0);
			weights.add(0f);
		}
	}
	
	private float saveTopWeights(final float[] topWeightsArray){
		float total = 0;
		for(int i=0;i<topWeightsArray.length;i++){
			topWeightsArray[i] = weights.get(i);
			total += topWeightsArray[i];
		}
		return total;
	}
	
	private void refillWeightList(final float[] topWeights, final float total){
		weights.clear();
		for (float topWeight : topWeights) {
			weights.add(Math.min(topWeight / total, 1));
		}
	}
	
	private void removeExcessJointIds(final int max){
		while(jointIds.size() > max){
			jointIds.remove(jointIds.size()-1);
		}
	}
	
	

}
