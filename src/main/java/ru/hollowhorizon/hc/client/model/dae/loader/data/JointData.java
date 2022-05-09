package ru.hollowhorizon.hc.client.model.dae.loader.data;

import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import java.util.ArrayList;
import java.util.List;


/**
 * Contains the extracted data for a single joint in the model. This stores the
 * joint's index, name, and local bind transform.
 * 
 * @author Karl
 *
 */
public class JointData {

    private final int      index;
    private final String   nameId;
    private final Matrix4f bindLocalTransform;

    private final List<JointData> children = new ArrayList<JointData>();

	public JointData(final int index, final String nameId, final Matrix4f bindLocalTransform) {
		this.index = index;
		this.nameId = nameId;
		this.bindLocalTransform = bindLocalTransform;
	}

	public void addChild(final JointData child) {
		children.add(child);
	}

    public int getIndex()
    {
        return index;
    }

    public String getNameId()
    {
        return nameId;
    }

    public Matrix4f getBindLocalTransform()
    {
        return bindLocalTransform;
    }

    public List<JointData> getChildren()
    {
        return children;
    }
}
