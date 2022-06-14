package ru.hollowhorizon.hc.client.model.smd;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.RegexPatterns;
import ru.hollowhorizon.hc.client.utils.math.VectorHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SmdAnimation {
    public final ValveStudioModel owner;
    public ArrayList<AnimFrame> frames = new ArrayList();
    public ArrayList<Bone> bones = new ArrayList();
    public int currentFrameIndex = 0;
    public int lastFrameIndex;
    public int totalFrames;
    public String animationName;
    private int frameIDBank = 0;

    public SmdAnimation(ValveStudioModel owner, String animationName, ResourceLocation resloc) {
        this.owner = owner;
        this.animationName = animationName;
        if (resloc.getPath().endsWith(".bmd")) {
            this.loadBmdModel(resloc);
        } else {
            this.loadSmdAnim(resloc);
        }

        this.setBoneChildren();
        this.reform();
    }

    public SmdAnimation(SmdAnimation anim, ValveStudioModel owner) {
        this.owner = owner;
        this.animationName = anim.animationName;

        for (Bone b : anim.bones) {
            this.bones.add(new Bone(b, b.parent != null ? (Bone) this.bones.get(b.parent.ID) : null, (SmdModel) null));
        }

        this.frames.addAll(anim.frames.stream().map((f) -> new AnimFrame(f, this)).collect(Collectors.toList()));
        this.totalFrames = anim.totalFrames;
    }

    private void loadSmdAnim(ResourceLocation resloc) {
        InputStream inputStream = HollowJavaUtils.getResource(resloc);
        String currentLine = null;
        int lineCount = 0;

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            Throwable var6 = null;

            try {
                while((currentLine = reader.readLine()) != null) {
                    ++lineCount;
                    if (!currentLine.startsWith("version")) {
                        if (currentLine.startsWith("nodes")) {
                            ++lineCount;

                            while(!(currentLine = reader.readLine()).startsWith("end")) {
                                ++lineCount;
                                this.parseBone(currentLine, lineCount);
                            }
                        } else if (currentLine.startsWith("skeleton")) {
                            this.startParsingAnimation(reader, lineCount, resloc);
                        }
                    }
                }
            } catch (Throwable var16) {
                var6 = var16;
                throw var16;
            } finally {
                if (reader != null) {
                    if (var6 != null) {
                        try {
                            reader.close();
                        } catch (Throwable var15) {
                            var6.addSuppressed(var15);
                        }
                    } else {
                        reader.close();
                    }
                }

            }

        } catch (IOException var18) {
            if (lineCount == -1) {
                throw new IllegalStateException("there was a problem opening the model file : " + resloc, var18);
            } else {
                throw new IllegalStateException("an error occurred reading the SMD file \"" + resloc + "\" on line #" + lineCount, var18);
            }
        }
    }

    private void loadBmdModel(ResourceLocation modelLoc) {
        BufferedInputStream inputStream = new BufferedInputStream(HollowJavaUtils.getResource(modelLoc));

        try {
            DataInputStream in = new DataInputStream(inputStream);
            Throwable var4 = null;

            try {
                byte version = in.readByte();

                assert version == 1;

                int numNodes = in.readShort();
                HollowJavaUtils.ensureIndex(this.bones, numNodes - 1);

                short time;
                for(int i = 0; i < numNodes; ++i) {
                    short boneID = in.readShort();
                    time = in.readShort();
                    String name = readNullTerm(in);
                    Bone parent = time != -1 ? this.bones.get(time) : null;
                    this.bones.set(boneID, new Bone(name, boneID, parent, null));
                }

                Map<Short, Map<Short, Matrix4f>> skeletonMap = new LinkedHashMap();
                int numSkeletons = this.totalFrames = in.readShort();

                for(time = 0; time < numSkeletons; ++time) {
                    skeletonMap.put(time, new LinkedHashMap());
                    Map<Short, Matrix4f> frame = (Map)skeletonMap.get(time);
                    int numBones = in.readShort();

                    for(int j = 0; j < numBones; ++j) {
                        short boneId = in.readShort();
                        float locX = in.readFloat();
                        float locY = in.readFloat();
                        float locZ = in.readFloat();
                        float rotX = in.readFloat();
                        float rotY = in.readFloat();
                        float rotZ = in.readFloat();
                        Matrix4f skeleton = VectorHelper.matrix4FromLocRot(locX, -locY, -locZ, rotX, -rotY, -rotZ);
                        frame.put(boneId, skeleton);
                    }

                    short i;
                    for(i = 0; i < numNodes; ++i) {
                        if (!frame.containsKey(i)) {
                            if (time == 0) {
                                throw new IOException("Missing bone definitions in first frame");
                            }

                            frame.put(i, skeletonMap.get((short)(time - 1)).get(i));
                        }
                    }

                    this.frames.add(time, new AnimFrame(this));

                    for(i = 0; i < frame.size(); ++i) {
                        this.frames.get(time).addTransforms(i, frame.get(i));
                    }
                }
            } catch (Throwable var29) {
                var4 = var29;
                throw var29;
            } finally {
                if (var4 != null) {
                    try {
                        in.close();
                    } catch (Throwable var28) {
                        var4.addSuppressed(var28);
                    }
                } else {
                    in.close();
                }

            }

        } catch (IOException var31) {
            throw new IllegalStateException(var31);
        }
    }

    private void parseBone(String line, int lineCount) {
        String[] params = line.split("\"");
        int id = Integer.parseInt(RegexPatterns.SPACE_SYMBOL.matcher(params[0]).replaceAll(""));
        String boneName = params[1];
        int parentID = Integer.parseInt(RegexPatterns.SPACE_SYMBOL.matcher(params[2]).replaceAll(""));
        Bone parent = parentID >= 0 ? (Bone)this.bones.get(parentID) : null;
        this.bones.add(id, new Bone(boneName, id, parent, (SmdModel)null));
        ValveStudioModel.print(boneName);
    }

    private void startParsingAnimation(BufferedReader reader, int count, ResourceLocation resloc) {
        int currentTime = 0;
        int lineCount = count + 1;
        String currentLine = null;

        try {

                while((currentLine = reader.readLine()) != null) {
                    ++lineCount;
                    String[] params = RegexPatterns.MULTIPLE_WHITESPACE.split(currentLine);
                    if (params[0].equalsIgnoreCase("time")) {
                        currentTime = Integer.parseInt(params[1]);
                        this.frames.add(currentTime, new AnimFrame(this));
                    } else {
                        if (currentLine.startsWith("end")) {
                            this.totalFrames = this.frames.size();
                            ValveStudioModel.print("Total number of frames = " + this.totalFrames);
                            return;
                        }

                        int boneIndex = Integer.parseInt(params[0]);
                        float[] locRots = new float[6];

                        for(int i = 1; i < 7; ++i) {
                            locRots[i - 1] = Float.parseFloat(params[i]);
                        }

                        Matrix4f animated = VectorHelper.matrix4FromLocRot(locRots[0], -locRots[1], -locRots[2], locRots[3], -locRots[4], -locRots[5]);
                        this.frames.get(currentTime).addTransforms(boneIndex, animated);
                    }
                }

        } catch (Exception var11) {
            throw new IllegalStateException("an error occurred reading the SMD file \"" + resloc + "\" on line #" + lineCount, var11);
        }
    }

    public int requestFrameID() {
        return this.frameIDBank++;
    }

    private void setBoneChildren() {
        for(int i = 0; i < this.bones.size(); ++i) {
            Bone theBone = this.bones.get(i);
            this.bones.stream().filter((child) -> child.parent == theBone).forEach(theBone::addChild);
        }

    }

    public void reform() {
        int rootID = this.owner.body.root.ID;

        for (AnimFrame frame : this.frames) {
            frame.fixUp(rootID, 0.0F);
            frame.reform();
        }

    }

    public void precalculateAnimation(SmdModel model) {

        for (AnimFrame frame : this.frames) {
            model.resetVerts();

            for (int j = 0; j < model.bones.size(); ++j) {
                Bone bone = model.bones.get(j);
                Matrix4f animated = frame.transforms.get(j);
                bone.preloadAnimation(frame, animated);
            }
        }

    }

    public int getNumFrames() {
        return this.frames.size();
    }

    public void setCurrentFrame(int i) {
        if (this.lastFrameIndex != i) {
            this.currentFrameIndex = i;
            this.lastFrameIndex = i;
        }

    }

    private static String readNullTerm(DataInputStream in) throws IOException {
        StringBuilder str = new StringBuilder();
        char ch = 0;

        do {
            if (ch != 0) {
                str.append(ch);
            }

            ch = in.readChar();
        } while(ch != 0);

        return str.toString();
    }
}
