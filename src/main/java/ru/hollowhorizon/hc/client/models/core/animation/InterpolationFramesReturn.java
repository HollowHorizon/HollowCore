package ru.hollowhorizon.hc.client.models.core.animation;

public class InterpolationFramesReturn {

    public AnimationFrame current;
    public AnimationFrame next;
    public float partialTick;

    public InterpolationFramesReturn(AnimationFrame current,
                                     AnimationFrame next,
                                     float partialTick){
        this.current = current;
        this.next = next;
        this.partialTick = partialTick;
    }
}
