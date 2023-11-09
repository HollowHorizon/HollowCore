package ru.hollowhorizon.hc.client.models.fbx;

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
            if (animation.animationName.equalsIgnoreCase(name)) {
                clearAnimations();
                this.currentAnimation = animation.copy(animation.animationName, animation.getAnimationId(), animation.nodes);
                for (FBXCurveNode node : this.currentAnimation.nodes) {
                    for (FBXMesh mesh : this.meshes) {
                        if (node.modelId == mesh.getModelId()) {
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

    @Override
    protected FBXModel clone() {
        return new FBXModel(meshes.clone(), animations.clone());
    }
}
