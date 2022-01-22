package ru.hollowhorizon.hc.client.render.entities;

public class HollowAnimationManager {
    private String animName = "";

    public void setAnimation(String animName) {
        if (!this.animName.equals(animName)) this.animName = animName;
    }

    public String getAnimName() {
        return animName;
    }
}
