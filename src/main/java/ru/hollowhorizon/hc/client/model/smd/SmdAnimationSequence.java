package ru.hollowhorizon.hc.client.model.smd;

import java.util.ArrayList;
import java.util.List;

public class SmdAnimationSequence {
    public boolean isLooped;
    String animName;
    List<SmdAnimation> sequence;
    int current = -1;

    public SmdAnimationSequence(String animType, List<SmdAnimation> animations, ValveStudioModel model, boolean isLooped) {
        this.animName = animType;
        this.sequence = new ArrayList<>();
        animations.forEach((animation) -> this.sequence.add(new SmdAnimation(animation, model)));
        this.isLooped = isLooped;
    }

    public SmdAnimation next() {
        ++this.current;
        if (this.current >= this.sequence.size()) {
            this.current = 0;
        }

        return this.sequence.isEmpty() ? null : this.sequence.get(this.current);
    }

    public SmdAnimation current() {
        if (this.current < 0) {
            this.current = 0;
        }

        return this.sequence.isEmpty() ? null : this.sequence.get(this.current);
    }

    public void precalculate(SmdModel body) {
        List<SmdAnimation> precalculated = new ArrayList<>();

        for (SmdAnimation animation : this.sequence) {
            if (!precalculated.contains(animation)) {
                animation.precalculateAnimation(body);
                precalculated.add(animation);
            }
        }

    }

    public SmdAnimation checkForIncrement(IncrementingVariable variable) {
        SmdAnimation current = this.current();
        if (this.sequence.size() > 1 && variable.value + variable.increment >= (float)current.totalFrames) {
            variable.value = 0.0F;
            return this.next();
        } else {
            return current;
        }
    }

    public SmdAnimation checkForFinalFrame(int frame) {
        return frame > this.current().totalFrames ? this.next() : this.current();
    }
}
