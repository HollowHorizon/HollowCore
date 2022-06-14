package ru.hollowhorizon.hc.client.model.smd;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.loading.FMLLoader;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.hollow_config.HollowCoreConfig;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.client.utils.RegexPatterns;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

public class ValveStudioModel extends Model {
    public static boolean debugModel = false;
    public SmdModel body;
    public SmdAnimationSequence currentSequence;
    public ResourceLocation resource;
    public boolean overrideSmoothShading;
    public boolean hasChanged;
    public HashMap<String, SmdAnimationSequence> anims;
    protected Bone root;
    protected String materialPath;
    protected boolean usesMaterials;
    ArrayList<Bone> allBones;
    private boolean hasAnimations;
    private float scale;

    public ValveStudioModel(ValveStudioModel model) {

        super(model.renderType);
        this.hasChanged = true;
        this.hasAnimations = false;
        this.anims = new HashMap<>(4);
        this.usesMaterials = false;
        this.scale = -1.0F;
        this.body = new SmdModel(model.body, this);

        for (Entry<String, SmdAnimationSequence> animation : model.anims.entrySet()) {
            this.anims.put(animation.getKey(), new SmdAnimationSequence(animation.getKey(), animation.getValue().sequence, this, true));
        }

        this.hasAnimations = model.hasAnimations;
        this.usesMaterials = model.usesMaterials;
        this.scale = model.scale;
        this.resource = model.resource;
        this.currentSequence = this.anims.get("idle");
        this.overrideSmoothShading = model.overrideSmoothShading;
    }

    public ValveStudioModel(ResourceLocation resource, boolean overrideSmoothShading) {
        super(RenderType::entityCutout);
        this.hasChanged = true;
        this.hasAnimations = false;
        this.anims = new HashMap<>(4);
        this.usesMaterials = false;
        this.scale = -1.0F;
        this.overrideSmoothShading = overrideSmoothShading;
        this.resource = resource;

        this.loadQC(resource);

        this.reformBones();

        this.precalculateAnimations();

    }

    public void updateAnimations(HashMap<String, ResourceLocation> newAnimations) {
        for(HashMap.Entry<String, ResourceLocation> entry : newAnimations.entrySet()) {
            List<SmdAnimation> sequence = new ArrayList<>();

            sequence.add(new SmdAnimation(this, entry.getKey(), entry.getValue()));

            this.anims.put(entry.getKey(), new SmdAnimationSequence(entry.getKey(), sequence, this, true));
        }
    }

    public ValveStudioModel(ResourceLocation resource) {
        this(resource, false);
    }

    public static void print(Object o) {
        if (debugModel) {
            System.out.println(o);
        }

    }

    public float getScale() {
        return this.scale;
    }

    public boolean hasAnimations() {
        return this.hasAnimations;
    }

    private void precalculateAnimations() {

        for (SmdAnimationSequence anim : this.anims.values()) {
            anim.precalculate(this.body);
        }

    }

    @Override
    public void renderToBuffer(MatrixStack matrix, IVertexBuilder buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        try {
            this.body.render(matrix, buffer, packedLight, packedOverlay, Minecraft.getInstance().getFrameTime(), red, green, blue, alpha);
            this.hasChanged = false;
        } catch (Exception var10) {
            var10.printStackTrace();
        }

    }

    void sendBoneData(SmdModel model) {
        this.allBones = model.bones;
        if (!model.isBodyGroupPart) {
            this.root = model.root;
        }

    }

    private void reformBones() {
        this.root.reformChildren();
        this.allBones.forEach(Bone::invertRestMatrix);
    }

    public void animate() {
        if (this.body.currentAnim == null) {
            this.setAnimation("idle");
        }

        if (HollowCoreConfig.is_smooth_animations.getValue()) {
            this.reverseFrame(this.body);
            this.resetVerts(this.body);
            this.root.setModified();

            for (Bone bone : this.allBones) bone.applyModified();

            this.applyVertChange(this.body);
            this.hasChanged = true;
            this.nextFrame(this.body);
        }

        this.resetVerts(this.body);
        this.root.setModified();

        for (Bone bone : this.allBones) bone.applyModified();

        this.applyVertChange(this.body);
        this.hasChanged = true;
    }

    private void reverseFrame(SmdModel model) {
        --model.currentAnim.currentFrameIndex;
        if (model.currentAnim.currentFrameIndex < 0) {
            if (this.currentSequence.isLooped) {
                model.currentAnim.currentFrameIndex = model.currentAnim.totalFrames - 1;
            } else {
                model.currentAnim.currentFrameIndex = 0;
            }
        }

    }

    private void nextFrame(SmdModel model) {
        if (!this.currentSequence.isLooped) {
            if (model.currentAnim.currentFrameIndex > 0) {
                ++model.currentAnim.currentFrameIndex;
            }
        } else {
            ++model.currentAnim.currentFrameIndex;
            if (model.currentAnim.currentFrameIndex == model.currentAnim.totalFrames) {
                model.currentAnim.currentFrameIndex = 0;
            }
        }

    }

    public void setAnimation(String animType) {
        if (this.anims.containsKey(animType)) {
            this.currentSequence = this.anims.get(animType);
        } else {
            this.currentSequence = this.anims.get("idle");
        }

        if (this.currentSequence != null) {
            this.body.setAnimation(this.currentSequence.current());
        } else {
            this.body.setAnimation(null);
        }

    }

    protected String getMaterialPath(String subFile) {
        String result = "/assets/hc";
        if (!this.materialPath.startsWith("/")) {
            result = result + "/";
        }

        result = result + this.materialPath;
        if (!subFile.startsWith("/")) {
            result = result + "/";
        }

        result = result + subFile;
        int lastDot = result.lastIndexOf(".");
        result = lastDot == -1 ? result + ".mat" : result.substring(0, lastDot) + ".mat";
        return result;
    }

    private void resetVerts(SmdModel model) {
        if (model != null) {
            model.verts.forEach(DeformVertex::reset);
        }
    }

    private void applyVertChange(SmdModel model) {
        if (model != null) {
            model.verts.forEach(DeformVertex::applyChange);
        }
    }

    private void loadQC(ResourceLocation resource) {
        HollowCore.LOGGER.info("start model 1");
        InputStream inputStream = HollowJavaUtils.getResource(resource);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String[] bodyParams = null;
            ArrayList<String[]> animParams = new ArrayList<>();

            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                String[] params = RegexPatterns.MULTIPLE_WHITESPACE.split(currentLine);
                if (params[0].equalsIgnoreCase("$body")) {
                    bodyParams = params;
                } else if (params[0].equalsIgnoreCase("$anim")) {
                    this.hasAnimations = true;
                    animParams.add(params);
                } else if (params[0].equalsIgnoreCase("$cdmaterials")) {
                    this.usesMaterials = true;
                    this.materialPath = params[1];
                } else if (params[0].equalsIgnoreCase("$scale")) {
                    this.scale = Float.parseFloat(params[1]);
                }
            }

            if (this.scale == -1.0F) {
                this.scale = 1.0F;
            }

            assert bodyParams != null;
            ResourceLocation modelPath = this.getResource(bodyParams[1]);
            this.body = new SmdModel(this, modelPath);
            HashMap<ResourceLocation, SmdAnimation> recognizedAnimations = new HashMap<>();


            for (String[] animPars : animParams) {
                ResourceLocation animPath = this.getResource(animPars[2]);
                if (!recognizedAnimations.containsKey(animPath)) {
                    SmdAnimation animation = new SmdAnimation(this, animPars[1], animPath);
                    recognizedAnimations.put(animPath, animation);
                }
            }

            for (String[] animPars : animParams) {
                String animName = animPars[1];
                List<SmdAnimation> sequence = new ArrayList<>();

                for (int i = 2; i < animPars.length; ++i) {
                    ResourceLocation animPath = this.getResource(animPars[i]);
                    if (recognizedAnimations.containsKey(animPath)) {
                        sequence.add(recognizedAnimations.get(animPath));
                    } else if (!FMLLoader.isProduction()) {
                        HollowCore.LOGGER.error("Animation file " + animPath + " was not registered in " + this.resource + "!");
                    }
                }

                this.anims.put(animName, new SmdAnimationSequence(animName, sequence, this, true));
                if (animName.equalsIgnoreCase("idle")) {
                    this.currentSequence = this.anims.get("idle");
                }
            }

        } catch (Exception var29) {
            throw new IllegalStateException("An error occurred while reading the " + resource.toString() + " PQC file", var29);
        }
    }

    public ResourceLocation getResource(String fileName) {
        String urlAsString = this.resource.getPath();
        int lastIndex = urlAsString.lastIndexOf(47);
        String startString = urlAsString.substring(0, lastIndex);
        return new ResourceLocation("hc", (startString + "/" + fileName).toLowerCase(Locale.ROOT));
    }


}
