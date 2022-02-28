package ru.hollowhorizon.hc.client.model.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;
import ru.hollowhorizon.hc.client.utils.tools.ModRenderTypes;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FBXModel implements Cloneable {
    private final FBXMesh[] meshes;
    private final FBXAnimation[] animations;
    private final FBXMaterial[] materials;
    private final List<FBXAnimation> currentAnimations = new ArrayList<>();

    public FBXModel(FBXMesh[] meshes, FBXAnimation[] animations, FBXMaterial[] materials) {
        this.meshes = meshes;
        this.animations = animations;
        this.materials = materials;
        linkMaterials();
        //calculateUVs();
    }

    public void calculateUVs() {
        for (FBXMesh mesh : meshes) {
            try {
                BufferedImage image = ImageIO.read(Minecraft.getInstance().getResourceManager().getResource(mesh.getMaterial().getTexture()).getInputStream());
                int w = image.getWidth();
                int h = image.getHeight();

                float[] uvs = mesh.getUvMap();
                float[] sizedUVs = new float[uvs.length];
                int size = uvs.length / 2;
                for (int i = 0; i < size; i++) {
                    float u = uvs[i * 2];
                    float v = uvs[i * 2 + 1];

                    sizedUVs[i * 2] = u * w;
                    sizedUVs[i * 2 + 1] = v * h;
                }
                mesh.setUvMap(sizedUVs);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void linkMaterials() {
        for (FBXMaterial material : materials) {
            for (FBXMesh mesh : meshes) {
                if (mesh.getModelId() == material.getModelId()) {
                    mesh.addMaterial(material);
                }
            }
        }
    }

    public List<FBXAnimation> getCurrentAnimations() {
        return currentAnimations;
    }

    public void linkAnimations() {
        clearAnimations();

        for (FBXAnimation checkAnim : this.currentAnimations) {
            for (FBXCurveNode node : checkAnim.getNodes()) {
                for (FBXMesh mesh : this.meshes) {
                    if (node.getModelId() == mesh.getModelId()) {
                        mesh.addAnimationData(node);
                    }
                }
            }
        }
    }

    public void clearAnimations() {
        for (FBXMesh mesh : this.meshes) {
            mesh.clearAnimations();
        }
    }

    public FBXAnimation[] getAnimations() {
        return animations;
    }

    public void updateAnimation(HollowAnimationManager manager) {
        currentAnimations.forEach(FBXAnimation::tickFrame);

        Iterator<FBXAnimation> iterator = currentAnimations.iterator();
        while (iterator.hasNext()) {
            FBXAnimation animation = iterator.next();
            if(animation.isEnd()) {
                for(String next : animation.getNextAnimations()) {
                    String[] data = next.split(":");
                    manager.addAnimation(data[0], Boolean.getBoolean(data[1]));
                }
                iterator.remove();
            }
        }
    }

    public void render(IRenderTypeBuffer builder, MatrixStack stack, int light) {
        for (FBXMesh mesh : meshes) {
            ResourceLocation texture = mesh.getMaterial().getTexture();

            mesh.render(builder.getBuffer(ModRenderTypes.getFBXModel(texture, mesh.mode)), stack, light);
        }
    }

    public FBXAnimation getAnimation(String name) {
        for (FBXAnimation animation : animations) {
            if (animation.getAnimationName().equals(name)) {
                return animation;
            }
        }
        return null;
    }

    @Override
    public FBXModel clone() {
        return new FBXModel(meshes.clone(), animations.clone(), materials);
    }
}
