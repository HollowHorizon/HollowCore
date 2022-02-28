package ru.hollowhorizon.hc.client.render.entities;

import ru.hollowhorizon.hc.client.model.fbx.FBXAnimation;
import ru.hollowhorizon.hc.client.model.fbx.FBXModel;

import java.util.List;

public class HollowAnimationManager {
    private final FBXModel model;

    public HollowAnimationManager(FBXModel model) {
        this.model = model;
    }

    public List<FBXAnimation> getAnimations() {
        return model.getCurrentAnimations();
    }

    public void stopAnimation(String animName) {
        this.model.getCurrentAnimations().removeIf(animation -> animation.getAnimationName().equals(animName));
        this.model.linkAnimations();
    }

    public void addAnimation(String animName, boolean isEndless, String[] nextAnimations) {
        for (FBXAnimation animation : this.model.getCurrentAnimations()) {
            if (animation.getAnimationName().equals(animName)) return;
        }
        this.model.getCurrentAnimations().add(this.model.getAnimation(animName).clone(isEndless).setNextAnimations(nextAnimations));
        this.model.linkAnimations();
    }

    public void addAnimation(String animName, boolean isEndless) {
        addAnimation(animName, isEndless, new String[]{});
    }
}
