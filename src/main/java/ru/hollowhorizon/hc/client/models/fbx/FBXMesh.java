/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.fbx;

import java.util.ArrayList;
import java.util.List;

public class FBXMesh {
    public final int mode;
    private final FBXVertex[] vertices;
    private final float[] normals;
    private final float[] uvMap;
    private final int indCount;
    private final int[] indices;
    private final long modelId;
    private final List<FBXCurveNode> animationData = new ArrayList<>();

    public FBXMesh(long modelId, double[] vertices, double[] normals, double[] uvMap, int[] indices, int mode) {
        this.vertices = FBXVertex.fromArray(vertices);
        this.normals = toFloatArray(normals);
        this.uvMap = toFloatArray(uvMap);
        this.indCount = indices.length;
        this.indices = indices;
        this.mode = mode;
        this.modelId = modelId;
    }

    public void addAnimationData(FBXCurveNode node) {
        this.animationData.add(node);
    }

    private float[] toFloatArray(double[] array) {
        int size = array.length;
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            floats[i] = (float) array[i];
        }
        return floats;
    }

    public void clearAnimations() {
        this.animationData.clear();
    }

    public long getModelId() {
        return modelId;
    }
}
