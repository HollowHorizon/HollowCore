package ru.hollowhorizon.hc.client.model.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.utils.tools.ModRenderTypes;

public class FBXModel implements Cloneable {
    private final FBXMesh[] meshes;
    private final FBXAnimation[] animations;
    private FBXAnimation currentAnimation;

    public FBXModel(FBXMesh[] meshes, FBXAnimation[] animations) {
        this.meshes = meshes;
        this.animations = animations;
        updateAnimation();
    }

    public FBXAnimation getCurrentAnimation() {
        return currentAnimation;
    }

    public void setAnimation(String name) {
        for (FBXAnimation animation : this.animations) {
            if (animation.getAnimationName().equalsIgnoreCase(name)) {
                clearAnimations();
                this.currentAnimation = animation.clone();
                for (FBXCurveNode node : this.currentAnimation.getNodes()) {
                    for (FBXMesh mesh : this.meshes) {
                        if (node.getModelId() == mesh.getModelId()) {
                            mesh.addAnimationData(node);
                        }
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

    public void updateAnimation() {
        if (currentAnimation == null) setAnimation("Scene");
        else currentAnimation.tickFrame();
    }

    public void render(IRenderTypeBuffer builder, MatrixStack stack, int light) {
        for (FBXMesh mesh : meshes) {
            mesh.render(builder.getBuffer(ModRenderTypes.getFBXModel(new ResourceLocation("minecraft:textures/block/cobblestone.png"), mesh.mode)), stack, light);
        }
    }

    @Override
    protected FBXModel clone() {
        return new FBXModel(meshes.clone(), animations.clone());
    }
}
