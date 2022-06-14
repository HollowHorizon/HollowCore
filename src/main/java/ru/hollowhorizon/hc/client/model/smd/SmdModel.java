package ru.hollowhorizon.hc.client.model.smd;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.hollow_config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.RegexPatterns;
import ru.hollowhorizon.hc.client.utils.math.VectorHelper;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SmdModel {
    public final ValveStudioModel owner;
    public ArrayList<NormalizedFace> faces = new ArrayList<>(0);
    public List<DeformVertex> verts = Collections.synchronizedList(new ArrayList<>(0));
    public ArrayList<Bone> bones = new ArrayList<>(0);
    public HashMap<String, Bone> nameToBoneMapping = new HashMap<>();
    public HashMap<String, Material> materialsByName;
    public HashMap<Material, ArrayList<NormalizedFace>> facesByMaterial;
    public SmdAnimation currentAnim;
    public Bone root;
    protected boolean isBodyGroupPart;
    int lineCount = -1;
    private int vertexIDBank = 0;

    SmdModel(SmdModel model, ValveStudioModel owner) {
        this.owner = owner;
        this.isBodyGroupPart = model.isBodyGroupPart;
        for (NormalizedFace face : model.faces) {
            int length = face.vertices.length;

            for (int i = 0; i < length; ++i) {
                DeformVertex d = new DeformVertex(face.vertices[i]);
                HollowJavaUtils.ensureIndex(this.verts, d.ID);
                this.verts.set(d.ID, d);
            }
        }

        for (NormalizedFace face : model.faces) {
            this.faces.add(new NormalizedFace(face, this.verts));
        }

        int i;
        Bone b;
        for (i = 0; i < model.bones.size(); ++i) {
            b = model.bones.get(i);
            this.bones.add(new Bone(b, null, this));
        }

        for (i = 0; i < model.bones.size(); ++i) {
            b = model.bones.get(i);
            b.copy.setChildren(b, this.bones);
        }

        this.root = model.root.copy;
        owner.sendBoneData(this);
    }

    SmdModel(ValveStudioModel owner, ResourceLocation resloc) {
        this.owner = owner;
        this.isBodyGroupPart = false;
        if (resloc.getPath().endsWith(".bmd")) {
            this.loadBmdModel(resloc);
        } else {
            this.loadSmdModel(resloc, null);
        }

        this.setBoneChildren();
        this.determineRoot();
        owner.sendBoneData(this);
        ValveStudioModel.print("Number of vertices = " + this.verts.size());
    }

    SmdModel(ValveStudioModel owner, ResourceLocation resloc, SmdModel body) {
        this.owner = owner;
        this.isBodyGroupPart = true;
        if (resloc.getPath().endsWith(".bmd")) {
            this.loadBmdModel(resloc);
        } else {
            this.loadSmdModel(resloc, body);
        }

        this.setBoneChildren();
        this.determineRoot();
        owner.sendBoneData(this);
    }

    private static String readNullTerm(DataInputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        char ch = 0;

        do {
            if (ch != 0) {
                str.append(ch);
            }

            ch = in.readChar();
        } while (ch != 0);

        return str.toString();
    }

    private void loadSmdModel(ResourceLocation resloc, SmdModel body) {
        BufferedInputStream inputStream = new BufferedInputStream(HollowJavaUtils.getResource(resloc));


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            this.lineCount = 0;

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                ++this.lineCount;
                if (!currentLine.startsWith("version")) {
                    if (currentLine.startsWith("nodes")) {
                        ++this.lineCount;

                        while (!(currentLine = reader.readLine()).startsWith("end")) {
                            ++this.lineCount;
                            this.parseBone(currentLine, body);
                        }

                        ValveStudioModel.print("Number of model bones = " + this.bones.size());
                    } else if (currentLine.startsWith("skeleton")) {
                        ++this.lineCount;
                        reader.readLine();
                        ++this.lineCount;

                        while (!(currentLine = reader.readLine()).startsWith("end")) {
                            ++this.lineCount;
                            if (!this.isBodyGroupPart) {
                                this.parseBoneValues(currentLine);
                            }
                        }
                    } else if (currentLine.startsWith("triangles")) {
                        ++this.lineCount;

                        while (!(currentLine = reader.readLine()).startsWith("end")) {
                            Material mat = this.owner.usesMaterials ? this.requestMaterial(currentLine) : null;
                            String[] params = new String[3];

                            for (int i = 0; i < 3; ++i) {
                                ++this.lineCount;
                                params[i] = reader.readLine();
                            }

                            this.parseFace(params, mat);
                        }
                    }
                }
            }
        } catch (Exception var20) {
            if (this.lineCount == -1) {
                throw new IllegalStateException("there was a problem opening the model file : " + resloc, var20);
            }

            throw new IllegalStateException("an error occurred reading the SMD file \"" + resloc + "\" on line #" + this.lineCount, var20);
        }

        ValveStudioModel.print("Number of faces = " + this.faces.size());
    }

    private void loadBmdModel(ResourceLocation modelLoc) {
        BufferedInputStream inputStream = new BufferedInputStream(HollowJavaUtils.getResource(modelLoc));

        try (DataInputStream in = new DataInputStream(inputStream)) {
            byte version = in.readByte();

            assert version == 1;

            int numNodes = in.readShort();
            HollowJavaUtils.ensureIndex(this.bones, numNodes - 1);

            short numMaterial;
            for (int i = 0; i < numNodes; ++i) {
                i = in.readShort();
                numMaterial = in.readShort();
                String name = readNullTerm(in);
                Bone parent = numMaterial != -1 ? this.bones.get(numMaterial) : null;
                this.bones.set(i, new Bone(name, i, parent, this));
            }

            int numSkeletons = in.readShort();

            float x;
            int vertexCount;
            short numTriangles;
            for (int i = 0; i < numSkeletons; ++i) {
                numMaterial = in.readShort();

                for (vertexCount = 0; vertexCount < numMaterial; ++vertexCount) {
                    numTriangles = in.readShort();
                    float locX = in.readFloat();
                    float locY = in.readFloat();
                    float locZ = in.readFloat();
                    float rotX = in.readFloat();
                    float rotY = in.readFloat();
                    x = in.readFloat();
                    Bone theBone = this.bones.get(numTriangles);
                    theBone.setRest(VectorHelper.matrix4FromLocRot(locX, -locY, -locZ, rotX, -rotY, -x));
                }
            }

            List<String> material = new ArrayList<>();
            numMaterial = in.readShort();

            for (vertexCount = 0; vertexCount < numMaterial; ++vertexCount) {
                material.add(readNullTerm(in));
            }

            vertexCount = 0;
            numTriangles = in.readShort();

            for (int i = 0; i < numTriangles; ++i) {
                String mat = material.get(in.readByte());
                DeformVertex[] faceVerts = new DeformVertex[3];
                TextureCoordinate[] uvs = new TextureCoordinate[3];

                for (int j = 0; j < 3; ++j) {
                    in.readShort();
                    x = in.readFloat();
                    float y = -in.readFloat();
                    float z = -in.readFloat();
                    float normX = in.readFloat();
                    float normY = -in.readFloat();
                    float normZ = -in.readFloat();
                    float u = in.readFloat();
                    float v = in.readFloat();
                    int id = vertexCount++;
                    DeformVertex dv = this.getExisting(x, y, z);
                    if (dv == null) {
                        if (HollowCoreConfig.is_smooth_animations.getValue()) {
                            faceVerts[j] = new DeformVertexSmooth(x, y, z, normX, normY, normZ, this.vertexIDBank);
                        } else {
                            faceVerts[j] = new DeformVertex(x, y, z, normX, normY, normZ, this.vertexIDBank);
                        }
                    } else {
                        faceVerts[j] = dv;
                    }

                    byte links = in.readByte();

                    for (int w = 0; w < links; ++w) {
                        int boneID = in.readShort();
                        float weight = in.readFloat();
                        this.bones.get(boneID).addVertex(faceVerts[j], weight);
                    }

                    HollowJavaUtils.ensureIndex(this.verts, id);
                    this.verts.set(id, faceVerts[j]);
                    uvs[j] = new TextureCoordinate(u, 1.0F - v);
                }

                NormalizedFace face = new NormalizedFace(faceVerts, uvs);
                face.vertices = faceVerts;
                face.textureCoordinates = uvs;
                this.faces.add(face);
                if (this.owner.usesMaterials) {
                    Material mater = this.requestMaterial(mat);
                    if (this.facesByMaterial == null) {
                        this.facesByMaterial = new HashMap<>();
                    }

                    ArrayList<NormalizedFace> list = this.facesByMaterial.computeIfAbsent(mater, (k) -> new ArrayList<>());
                    list.add(face);
                }
            }
        } catch (IOException var42) {
            throw new IllegalStateException("An error occurred while reading BMD " + modelLoc.toString(), var42);
        }
    }

    private Material requestMaterial(String materialName) {
        if (!this.owner.usesMaterials) {
            return null;
        } else {
            if (this.materialsByName == null) {
                this.materialsByName = new HashMap<>();
            }

            Material result = this.materialsByName.get(materialName);
            if (result != null) {
                return result;
            } else {
                String materialPath = this.owner.getMaterialPath(materialName);
                URL materialURL = SmdModel.class.getResource(materialPath);

                try {
                    File materialFile = new File(materialURL.toURI());
                    result = new Material(materialFile);
                    this.materialsByName.put(materialName, result);
                    return result;
                } catch (Exception var6) {
                    throw new IllegalStateException(var6);
                }
            }
        }
    }

    private void parseBone(String line, SmdModel body) {
        String[] params = line.split("\"");
        int id = Integer.parseInt(RegexPatterns.SPACE_SYMBOL.matcher(params[0]).replaceAll(""));
        String boneName = params[1];
        Bone theBone = body != null ? body.getBoneByName(boneName) : null;
        if (theBone == null) {
            int parentID = Integer.parseInt(RegexPatterns.SPACE_SYMBOL.matcher(params[2]).replaceAll(""));
            Bone parent = parentID >= 0 ? this.bones.get(parentID) : null;
            theBone = new Bone(boneName, id, parent, this);
        }

        HollowJavaUtils.ensureIndex(this.bones, id);
        this.bones.set(id, theBone);
        this.nameToBoneMapping.put(boneName, theBone);
        ValveStudioModel.print(boneName);
    }

    private void parseBoneValues(String line) {
        String[] params = RegexPatterns.MULTIPLE_WHITESPACE.split(line);
        int id = Integer.parseInt(params[0]);
        float[] locRots = new float[6];

        for (int i = 1; i < 7; ++i) {
            locRots[i - 1] = Float.parseFloat(params[i]);
        }

        Bone theBone = this.bones.get(id);
        theBone.setRest(VectorHelper.matrix4FromLocRot(locRots[0], -locRots[1], -locRots[2], locRots[3], -locRots[4], -locRots[5]));
    }

    private void parseFace(String[] params, Material mat) {
        DeformVertex[] faceVerts = new DeformVertex[3];
        TextureCoordinate[] uvs = new TextureCoordinate[3];

        for (int i = 0; i < 3; ++i) {
            String[] values = RegexPatterns.MULTIPLE_WHITESPACE.split(params[i]);
            float x = Float.parseFloat(values[1]);
            float y = -Float.parseFloat(values[2]);
            float z = -Float.parseFloat(values[3]);
            float xn = Float.parseFloat(values[4]);
            float yn = -Float.parseFloat(values[5]);
            float zn = -Float.parseFloat(values[6]);
            DeformVertex v = this.getExisting(x, y, z);
            if (v == null) {
                if (HollowCoreConfig.is_smooth_animations.getValue()) {
                    faceVerts[i] = new DeformVertexSmooth(x, y, z, xn, yn, zn, this.vertexIDBank);
                } else {
                    faceVerts[i] = new DeformVertex(x, y, z, xn, yn, zn, this.vertexIDBank);
                }

                HollowJavaUtils.ensureIndex(this.verts, this.vertexIDBank);
                this.verts.set(this.vertexIDBank, faceVerts[i]);
                ++this.vertexIDBank;
            } else {
                faceVerts[i] = v;
            }

            uvs[i] = new TextureCoordinate(Float.parseFloat(values[7]), 1.0F - Float.parseFloat(values[8]));
            if (values.length > 10) {
                this.doBoneWeights(values, faceVerts[i]);
            }
        }

        NormalizedFace face = new NormalizedFace(faceVerts, uvs);
        face.vertices = faceVerts;
        face.textureCoordinates = uvs;
        this.faces.add(face);
        if (mat != null) {
            if (this.facesByMaterial == null) {
                this.facesByMaterial = new HashMap<>();
            }

            ArrayList<NormalizedFace> list = this.facesByMaterial.computeIfAbsent(mat, k -> new ArrayList<>());

            list.add(face);
        }

    }

    private DeformVertex getExisting(float x, float y, float z) {
        for (DeformVertex vertex : this.verts) {
            if (vertex.equals(x, y, z)) return vertex;
        }
        return null;
    }

    private void doBoneWeights(String[] values, DeformVertex vert) {
        int links = Integer.parseInt(values[9]);
        float[] weights = new float[links];
        float sum = 0.0F;

        int i;
        for (i = 0; i < links; ++i) {
            weights[i] = Float.parseFloat(values[i * 2 + 11]);
            sum += weights[i];
        }

        for (i = 0; i < links; ++i) {
            int boneID = Integer.parseInt(values[i * 2 + 10]);
            float weight = weights[i] / sum;
            this.bones.get(boneID).addVertex(vert, weight);
        }

    }

    private void setBoneChildren() {
        for (int i = 0; i < this.bones.size(); ++i) {
            Bone theBone = this.bones.get(i);
            this.bones.stream().filter((child) -> child.parent == theBone).forEach(theBone::addChild);
        }

    }

    private void determineRoot() {
        for (Bone bone : this.bones) {
            if (bone.parent == null && !bone.children.isEmpty()) {
                this.root = bone;
                return;
            }
        }

        if (this.root == null) {
            for (Bone bone : this.bones) {
                if (bone.name.equals("blender_implicit")) {
                    this.root = bone;
                    return;
                }
            }
        }

    }

    public void setAnimation(SmdAnimation anim) {
        this.currentAnim = anim;
    }

    public Bone getBoneByID(int id) {
        try {
            return this.bones.get(id);
        } catch (IndexOutOfBoundsException var3) {
            return null;
        }
    }

    public Bone getBoneByName(String name) {
        for (Bone bone : bones) {
            if (bone.name.equals(name)) return bone;
        }

        return null;
    }

    public AnimFrame currentFrame() {
        return this.currentAnim == null ? null : (this.currentAnim.frames == null ? null : (this.currentAnim.frames.isEmpty() ? null : this.currentAnim.frames.get(this.currentAnim.currentFrameIndex)));
    }

    public void resetVerts() {
        this.verts.forEach(DeformVertex::reset);
    }

    public void render(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float partialTick, float r, float g, float b, float a) {
        boolean smooth = HollowCoreConfig.is_smooth_animations.getValue();
        if (this.owner.overrideSmoothShading) {
            smooth = false;
        }

        BufferBuilder bufferBuilder = (BufferBuilder) buffer;
        this.buildVBO(matrixStack, bufferBuilder, packedLight, packedOverlay, smooth, partialTick, r, g, b, a);
    }

    private void buildVBO(MatrixStack matrixStack, BufferBuilder builder, int packedLight, int packedOverlay, boolean smoothShading, float partialTick, float r, float g, float b, float a) {
        for (NormalizedFace face : this.faces) {
            face.addFaceForRender(matrixStack, builder, packedLight, packedOverlay, smoothShading, partialTick, r, g, b, a);
        }

    }
}