package ru.hollowhorizon.hc.client.models.core.bonemf;

import net.minecraft.util.Tuple;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BoneMFVertex {
    public double x;
    public double y;
    public double z;
    public double nX;
    public double nY;
    public double nZ;
    public double u;
    public double v;
    public List<Tuple<String, Double>> boneWeights;

    public BoneMFVertex(double x, double y, double z, double nX, double nY, double nZ, double u, double v){
        this.x = x;
        this.y = y;
        this.z = z;
        this.nX = nX;
        this.nY = nY;
        this.nZ = nZ;
        this.u = u;
        this.v = v;
        this.boneWeights = new ArrayList<>();

    }

    @Override
    public String toString() {
        StringBuilder bones = new StringBuilder();
        Iterator<Tuple<String, Double>> iter = boneWeights.iterator();
        while(iter.hasNext())
        {
            Tuple<String, Double> bone = iter.next();
            bones.append(String.format("'%s' : %f", bone.getA(), bone.getB()));
            if(iter.hasNext()){
                bones.append(",");
            }
        }
        return String.format("<BoneMFVertex x=%f, y=%f, z=%f, u=%f, v=%f, nX=%f, nY=%f, nZ=%f, weights='%s'>",
                x, y, z, u, v, nX, nY, nZ, bones);
    }

    public void addBoneWeight(String boneName, double weight){
        boneWeights.add(new Tuple<>(boneName, weight));
    }
}
