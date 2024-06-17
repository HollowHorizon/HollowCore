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

import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXProperty;
import ru.hollowhorizon.hc.client.models.fbx.raw.HollowByteStream;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FBXLoader {

    public static void main(String[] args) {
        var model = createModel(ResourceLocation.fromNamespaceAndPath(HollowCore.MODID, "models/entity/player_model.fbx"));

        System.out.println(model);
    }

    public static FBXModel createModel(ResourceLocation location) {
        FBXElement[] elements = loadRaw(location);
        FBXMesh[] meshes = loadMeshes(elements);

        FBXAnimation[] animations = loadAnimations(elements, meshes);
        return new FBXModel(meshes, animations);
    }

    public static FBXAnimation[] loadAnimations(FBXElement[] elements, FBXMesh[] meshes) {
        List<FBXAnimation> animations = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("AnimationStack")) {
                        long id = object.getProperties()[0].getData();
                        String name = object.getProperties()[1].getData();
                        if(name.indexOf('|')>0) name = name.substring(name.indexOf('|')+1);

                        long animLayer = getElementsByValue(elements, id)[0];

                        long[] animNodes = getElementsByValue(elements, animLayer);

                        List<FBXCurveNode> nodes = new ArrayList<>();
                        for (long animNode : animNodes) {
                            nodes.add(loadNode(elements, animNode, getModelId(elements, animNode, meshes)));
                        }
                        animations.add(new FBXAnimation(name, id, nodes));
                    }
                }
            }
        }
        return animations.toArray(new FBXAnimation[0]);
    }

    public static FBXCurveNode loadNode(FBXElement[] elements, long animNode, long modelId) {
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("AnimationCurveNode")) {
                        long id = object.getProperties()[0].getData();
                        if (id == animNode) {
                            String data = object.getProperties()[1].getData();

                            FBXCurveNode.CurveType type = switch (data.charAt(data.length() - 1)) {
                                case 'S' -> FBXCurveNode.CurveType.SCALING;
                                case 'T', 'P' -> FBXCurveNode.CurveType.TRANSLATION;
                                case 'R' -> FBXCurveNode.CurveType.ROTATION;
                                default -> null;
                            };

                            long xKey = getKeyFrames(elements, 'X', id);
                            long yKey = getKeyFrames(elements, 'Y', id);
                            long zKey = getKeyFrames(elements, 'Z', id);

                            boolean isRot = type == FBXCurveNode.CurveType.ROTATION;
                            FBXKeyFrame xFrames = parseKeyframes(elements, xKey, isRot);
                            FBXKeyFrame yFrames = parseKeyframes(elements, yKey, isRot);
                            FBXKeyFrame zFrames = parseKeyframes(elements, zKey, isRot);

                            return new FBXCurveNode(xFrames, yFrames, zFrames, type, modelId);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static FBXKeyFrame parseKeyframes(FBXElement[] elements, long id, boolean isRot) {
        if(id==-1) return null;
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("AnimationCurve")) {
                        long cid = object.getProperties()[0].getData();
                        if (cid == id) {
                            for (FBXElement curve : object.getElements()) {
                                if (curve.getName().equals("KeyValueFloat")) {
                                    return new FBXKeyFrame(curve.getProperties()[0].getData(), isRot);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static FBXMesh[] loadMeshes(FBXElement[] elements) {
        List<FBXMesh> meshes = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("Geometry")) {
                        long geometryId = object.getProperties()[0].getData();
                        long modelId = getValueByElement(elements, geometryId);
                        double[] verticesBuffer = object.getElementByName("Vertices").getProperties()[0].getData();
                        double[] normalsBuffer = object.getElementByName("LayerElementNormal").getElementByName("Normals").getProperties()[0].getData();
                        double[] uvs = object.getElementByName("LayerElementUV").getElementByName("UV").getProperties()[0].getData();


                        int[] indices = object.getElementByName("PolygonVertexIndex").getProperties()[0].getData();

                        double[] uvsBuffer;
                        int faceSize = 0;

                        FBXElement uvIndexesElement = object.getElementByName("LayerElementUV").getElementByName("UVIndex");
                        if(uvIndexesElement == null) {
                            faceSize = 3;
                            uvsBuffer = object.getElementByName("LayerElementUV").getElementByName("UV").getProperties()[0].getData();
                        }
                        else {
                            int[] uvIndices = uvIndexesElement.getProperties()[0].getData();

                            for (int i = 0; i < indices.length; i++) {
                                if (indices[i] < 0) {
                                    indices[i] = -indices[i] - 1;

                                    if (faceSize == 0) {
                                        faceSize = i + 1;
                                    }
                                }
                            }

                            uvsBuffer = generateUVs(uvIndices, indices, uvs);
                        }

                        int mode;
                        if (faceSize == 3) {
                            mode = GL11.GL_TRIANGLES;
                        } else {
                            mode = GL11.GL_QUADS;
                        }

                        meshes.add(new FBXMesh(modelId, verticesBuffer, normalsBuffer, uvsBuffer, indices, mode));
                    }
                }
            }
        }
        return meshes.toArray(new FBXMesh[0]);

    }

    private static long[] getElementsByValue(FBXElement[] elements, long id) {
        List<Long> elementIds = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Connections")) {
                for (FBXElement connection : element.getElements()) {
                    if (connection.getName().equals("C")) {
                        long checkId = connection.getProperties()[2].getData();
                        if (checkId == id) elementIds.add(connection.getProperties()[1].getData());
                    }
                }
            }
        }
        long[] res = new long[elementIds.size()];
        for (int i = 0; i < elementIds.size(); i++) {
            res[i] = elementIds.get(i);
        }
        return res;
    }

    public static long getKeyFrames(FBXElement[] elements, char type, long id) {
        for (FBXElement element : elements) {
            if (element.getName().equals("Connections")) {
                for (FBXElement connection : element.getElements()) {
                    if (connection.getName().equals("C") && connection.getProperties().length > 3) {
                        long checkId = connection.getProperties()[2].getData();
                        if (checkId == id) {
                            char ctype = ((String) connection.getProperties()[3].getData()).charAt(2);
                            switch (type) {
                                case 'X':
                                    if(ctype=='X') return connection.getProperties()[1].getData();
                                    break;
                                case 'Y':
                                    if(ctype=='Y') return connection.getProperties()[1].getData();
                                    break;
                                case 'Z':
                                    if(ctype=='Z') return connection.getProperties()[1].getData();
                                    break;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    public static long getModelId(FBXElement[] elements, long id, FBXMesh[] meshes) {
        for (FBXElement element : elements) {
            if (element.getName().equals("Connections")) {
                for (FBXElement connection : element.getElements()) {
                    if (connection.getName().equals("C")) {
                        long checkId = connection.getProperties()[1].getData();
                        long dataId = connection.getProperties()[2].getData();

                        if (checkId == id) {
                            for (FBXMesh mesh : meshes) {
                                if (mesh.getModelId() == dataId) return dataId;
                            }
                        }
                    }
                }
            }
        }
        return -1;
    }

    private static long getValueByElement(FBXElement[] elements, long id) {
        for (FBXElement element : elements) {
            if (element.getName().equals("Connections")) {
                for (FBXElement connection : element.getElements()) {
                    if (connection.getName().equals("C")) {
                        long checkId = connection.getProperties()[1].getData();
                        if (checkId == id) return connection.getProperties()[2].getData();
                    }
                }
            }
        }
        return -1;
    }

    private static double[] generateUVs(int[] UVIndices, int[] indices, double[] uvs) {
        if (UVIndices == null || UVIndices.length == 0) {
            return uvs;
        } else {
            double[] uvResult = new double[UVIndices.length * 2];

            for (int i = 0; i < indices.length; i++) {
                int ind = indices[i];
                if (ind < 0) ind = -ind;
                uvResult[ind * 2] = uvs[UVIndices[i] * 2];
                uvResult[ind * 2 + 1] = uvs[UVIndices[i] * 2 + 1];
            }

            return uvResult;
        }
    }

    public static int[] triangulate(int[] indices) {
        int[] newIndices = new int[(int) (indices.length * 1.5f)];
        int newIndexCounter = 0;

        for (int i = 0; i < indices.length - 4; i += 4) {
            newIndices[newIndexCounter++] = indices[i];
            newIndices[newIndexCounter++] = indices[i + 1];
            newIndices[newIndexCounter++] = indices[i + 2];
            newIndices[newIndexCounter++] = indices[i + 2];
            newIndices[newIndexCounter++] = indices[i + 3];
            newIndices[newIndexCounter++] = indices[i];
        }

        return newIndices;

    }

    public static FBXElement[] loadRaw(ResourceLocation location) {
        InputStream stream = HollowJavaUtils.getResource(location);

        try {
            //InputStream stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
            HollowByteStream reader = new HollowByteStream(stream);

            reader.read(23);
            int version = reader.readUInt();

            List<FBXElement> elements = new ArrayList<>();
            while (true) {
                FBXElement element = readElement(reader);
                if (element == null) {
                    break;
                } else {
                    elements.add(element);
                }
            }
            return elements.toArray(new FBXElement[0]);
        } catch (IOException e) {
            e.printStackTrace();
            return new FBXElement[0];
        }
    }

    public static FBXElement readElement(HollowByteStream reader) throws IOException {
        int offset = reader.readUInt();
        if (offset == 0 || reader.size() < offset) return null;
        int propertiesCount = reader.readUInt();
        reader.readUInt();

        String nodeName = reader.readString();

        if (propertiesCount > 1000) {
            throw new IOException("Too many properties");
        }
        FBXProperty<?>[] properties = new FBXProperty[propertiesCount];
        for (int i = 0; i < propertiesCount; i++) {
            char type = reader.readChar();
            properties[i] = FBXProperty.load(reader, type);
        }

        List<FBXElement> elements = new ArrayList<>();
        if (reader.available() < offset) {
            while (reader.available() < (offset - 13)) {
                elements.add(readElement(reader));
            }
            reader.read(13);
        }

        return new FBXElement(nodeName, properties, elements.toArray(new FBXElement[0]));
    }


}
