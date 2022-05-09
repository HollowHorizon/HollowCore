package ru.hollowhorizon.hc.client.model.fbx;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.client.model.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.model.fbx.raw.FBXProperty;
import ru.hollowhorizon.hc.client.model.fbx.raw.HollowByteStream;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FBXModelLoader {

    public static void main(String[] args) {
        FBXModel model = createModel(new ResourceLocation("hc:models/monster_quad.fbx"));

    }

    public static FBXModel createModel(ResourceLocation location) {
        FBXElement[] elements = loadRaw(location);
        FBXMesh[] meshes = loadMeshes(elements);
        //Collection<ResourceLocation> matLoc = Minecraft.getInstance().getResourceManager().listResources("materials", (material) -> material.endsWith(".png"));
        Collection<ResourceLocation> matLoc = new ArrayList<>();
        FBXMaterial[] materials = loadMaterials(elements, matLoc);


        FBXAnimation[] animations = loadAnimations(elements, meshes);
        return new FBXModel(meshes, animations, materials);
    }

    private static FBXMaterial[] loadMaterials(FBXElement[] elements, Collection<ResourceLocation> matLoc) {
        List<FBXMaterial> materials = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("Material")) {
                        long modelId = object.getProperties()[0].getData();
                        String name = object.getProperties()[1].getData();
                        name = name.substring(0, name.indexOf('\u0001') - 1);

                        FBXMaterial material = new FBXMaterial(modelId, name);
                        ResourceLocation matLocation = new ResourceLocation("minecraft:textures/block/dirt.png");
                        for (ResourceLocation mat : matLoc) {
                            String matName = mat.getPath().substring(0, mat.getPath().indexOf(".png") - 1);
                            if (matName.equals(name)) {
                                matLocation = mat;
                                break;
                            }
                        }
                        material.setMaterialLocation(matLocation);
                        materials.add(material);
                    }
                }
            }
        }
        return materials.toArray(new FBXMaterial[0]);
    }

    public static FBXAnimation[] loadAnimations(FBXElement[] elements, FBXMesh[] meshes) {
        List<FBXAnimation> animations = new ArrayList<>();
        for (FBXElement element : elements) {
            if (element.getName().equals("Objects")) {
                for (FBXElement object : element.getElements()) {
                    if (object.getName().equals("AnimationStack")) {
                        long id = object.getProperties()[0].getData();
                        String name = object.getProperties()[1].getData();
                        name = name.substring(0, name.indexOf('\u0001') - 1);
                        if (name.indexOf('|') > 0) name = name.substring(name.indexOf('|') + 1);

                        long animLayer = getElementsByValue(elements, id)[0];

                        long[] animNodes = getElementsByValue(elements, animLayer);

                        List<FBXCurveNode> nodes = new ArrayList<>();
                        for (long animNode : animNodes) {
                            nodes.add(loadNode(elements, animNode, getModelId(elements, animNode, meshes)));
                        }
                        animations.add(new FBXAnimation(name, id, nodes.toArray(new FBXCurveNode[0])));
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

                            FBXCurveNode.CurveType type = null;
                            switch (data.charAt(0)) {
                                case 'S':
                                    type = FBXCurveNode.CurveType.SCALING;
                                    break;
                                case 'T':
                                    type = FBXCurveNode.CurveType.TRANSLATION;
                                    break;
                                case 'R':
                                    type = FBXCurveNode.CurveType.ROTATION;
                                    break;
                            }

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
        if (id == -1) return null;
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
                        int[] uvIndices = object.getElementByName("LayerElementUV").getElementByName("UVIndex").getProperties()[0].getData();

                        int faceSize = 0;

                        for (int i = 0; i < indices.length; i++) {
                            if (indices[i] < 0) {
                                indices[i] = -indices[i] - 1;

                                if (faceSize == 0) {
                                    faceSize = i + 1;
                                }
                            }
                        }


                        int mode;
                        if (faceSize == 3) {
                            mode = GL11.GL_TRIANGLES;
                        } else {
                            mode = GL11.GL_QUADS;
                        }

                        meshes.add(new FBXMesh(modelId, verticesBuffer, normalsBuffer, uvs, uvIndices, indices, mode));
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
                                    if (ctype == 'X') return connection.getProperties()[1].getData();
                                    break;
                                case 'Y':
                                    if (ctype == 'Y') return connection.getProperties()[1].getData();
                                    break;
                                case 'Z':
                                    if (ctype == 'Z') return connection.getProperties()[1].getData();
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
                int uvInd = UVIndices[i];
                uvResult[ind * 2] = uvs[uvInd * 2];
                uvResult[ind * 2 + 1] = uvs[uvInd * 2 + 1];
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


        try {
            InputStream stream;
            try {
                stream = Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
            } catch (Exception ex) {
                stream = HollowJavaUtils.getResource(location);
            }
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
        if (offset == 0) return null;
        int propertiesCount = reader.readUInt();
        reader.readUInt();

        String nodeName = reader.readString();

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
