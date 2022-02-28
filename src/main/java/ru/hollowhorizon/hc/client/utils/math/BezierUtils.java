package ru.hollowhorizon.hc.client.utils.math;

import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BezierUtils {
    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            float t = i / 100F;

            System.out.println(calculatePoint4p(
                    t,
                    new Vector3f(0F, 0F, 0F),
                    new Vector3f(1F, 0.5F, 0F),
                    new Vector3f(1F, 1F, 1F),
                    new Vector3f(1F, 1F, 1F)
            ));
        }
    }

    public static Vector3f calculatePoint4p(float t, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3) {
        if (t == 0) t = 0.0000000001F;
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;

        Vector3f p = new Vector3f(p0.x() * uuu, p0.y() * uuu, p0.z() * uuu);
        p.add(new Vector3f(p1.x() * 3 * uu * t, p1.y() * 3 * uu * t, p1.z() * 3 * uu * t));
        p.add(new Vector3f(p2.x() * 3 * u * tt, p2.y() * 3 * u * tt, p2.z() * 3 * u * tt));
        p.add(new Vector3f(p3.x() * ttt, p3.y() * ttt, p3.z() * ttt));

        return p;
    }

    public static Vector3f calculatePoint(float t, List<Vector3f> vectors) {
        float sumX = calculatePointValue(getListOfX(vectors), t);
        float sumY = calculatePointValue(getListOfY(vectors), t);
        float sumZ = calculatePointValue(getListOfZ(vectors), t);
        return new Vector3f(sumX, sumY, sumZ);
    }

    private static ArrayList<Float> getListOfX(List<Vector3f> vectors) {
        ArrayList<Float> list = new ArrayList<>();
        for (Vector3f vector : vectors) {
            list.add(vector.x());
        }
        return list;
    }

    private static ArrayList<Float> getListOfY(List<Vector3f> vectors) {
        ArrayList<Float> list = new ArrayList<>();
        for (Vector3f vector : vectors) {
            list.add(vector.y());
        }
        return list;
    }

    private static ArrayList<Float> getListOfZ(List<Vector3f> vectors) {
        ArrayList<Float> list = new ArrayList<>();
        for (Vector3f vector : vectors) {
            list.add(vector.z());
        }
        return list;
    }

    private static int fact(int n) {
        if (n == 0) {
            return 1;
        } else {
            for (int i = n - 1; i > 0; i--) {
                n *= i;
            }
            return n;
        }
    }

    private static float calculatePointValue(ArrayList<Float> values, double t) {
        float restemp = 0;

        for (int i = 0; i < values.size(); i++) {
            restemp
                    += values.get(i) * combinaciones(values.size() - 1, i)
                    * Math.pow(t, i) * Math.pow((1 - t),
                    (values.size() - 1 - i));
        }

        return restemp;
    }

    private static int combinaciones(int n, int m) {
        return fact(n) / (fact(m) * fact(n - m));
    }
}
