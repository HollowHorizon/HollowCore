package ru.hollowhorizon.hc.client.models.core.animation;

public class AnimationWeight {

    public IPose provider;
    public float weight;

    public AnimationWeight(IPose provider, float weight){
        this.provider = provider;
        this.weight = weight;
    }
}
