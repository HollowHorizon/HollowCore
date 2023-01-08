package ru.hollowhorizon.hc.client.gltf.animation;

import java.util.Arrays;

public abstract class InterpolatedChannel {

    /**
     * The key frame times, in seconds
     */
    protected final float[] timesS;

    public InterpolatedChannel(float[] timesS) {
        this.timesS = timesS;
    }

    public float[] getKeys() {
        return timesS;
    }

    public boolean isEnd(float time) {
        return time >= timesS[timesS.length - 1];
    }

    public abstract void update(float timeS);

    protected abstract float[] getListener();

    public static int computeIndex(float key, float[] keys) {
        int index = Arrays.binarySearch(keys, key);
        if (index >= 0) {
            return index;
        }
        return Math.max(0, -index - 2);
    }

}
