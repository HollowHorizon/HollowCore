package ru.hollowhorizon.hc.api.utils;

import ru.hollowhorizon.hc.client.render.entities.HollowAnimationManager;

public interface IAnimatedEntity {
    void processAnimation();

    void setAnimation(String animation);

    void setCustomAnimation(String animation);

    void endCustomAnimation();

    boolean isCustomAnimation();

    HollowAnimationManager getManager();
}
