package ru.hollowhorizon.hc.client.model.dae.loader.data;

import java.util.List;

public class SkinningData {

    private final List<String>         jointOrder;
    private final List<VertexSkinData> verticesSkinData;
	
	public SkinningData(final List<String> jointOrder, final List<VertexSkinData> verticesSkinData){
		this.jointOrder = jointOrder;
		this.verticesSkinData = verticesSkinData;
	}

    public List<String> getJointOrder()
    {
        return jointOrder;
    }

    public List<VertexSkinData> getVerticesSkinData()
    {
        return verticesSkinData;
    }
}
