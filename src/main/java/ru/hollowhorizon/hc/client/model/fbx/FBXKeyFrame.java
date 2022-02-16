package ru.hollowhorizon.hc.client.model.fbx;

import java.util.Arrays;

public class FBXKeyFrame {
    private final float[] values;

    public FBXKeyFrame(float[] values, boolean isRot) {
//        if (!isRot) {
//            for (int i = 0; i < values.length; i++) {
//                values[i] = values[i] / 100F;
//            }
//        }


        this.values = values;
    }

    public static void main(String[] args) {
        FBXKeyFrame frame = new FBXKeyFrame(new float[]{1.0f, 2.0f, 3.0f}, true);

        System.out.println(Arrays.toString(frame.values));
    }

    public float[] getValues() {
        return values;
    }
}
