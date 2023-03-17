package ru.hollowhorizon.hc.client.models.core.bonemf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoneMFMeshAttribute extends BoneMFAttribute {

    private final List<BoneMFVertex> vertices;

    private final List<Integer> triangles;

    public BoneMFMeshAttribute(BoneMFNode owner){
        super(AttributeTypes.MESH, owner);
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
    }

    public void addTriangle(int a, int b, int c){
        this.triangles.add(a);
        this.triangles.add(b);
        this.triangles.add(c);
    }

    public void addVertex(BoneMFVertex vertex){
        this.vertices.add(vertex);
    }

    public List<BoneMFVertex> getVertices() {
        return vertices;
    }

    public List<Integer> getTriangles() {
        return triangles;
    }

    @Override
    public String toString() {
        StringBuilder indices  = new StringBuilder();
        Iterator<Integer> iter = triangles.iterator();
        while(iter.hasNext())
        {
            indices.append(iter.next());
            if(iter.hasNext()){
                indices.append(",");
            }
        }
        StringBuilder verts = new StringBuilder();
        for (BoneMFVertex vert : vertices) {
            verts.append(vert.toString());
            if (iter.hasNext()) {
                verts.append(",");
            }
        }
        return String.format("<BoneMFMeshAttribute name='%s' triangles='%s' vertices='%s'",
                getOwner().getName(), indices.toString(), verts.toString());
    }
}
