package ru.hollowhorizon.hc.client.model.fbx;

public class FBXAnimation implements Cloneable {
    private final String animationName;
    private final long animationId;
    private final FBXCurveNode[] nodes;
    private int currentFrame;
    private final boolean isEndless;
    private boolean isEnd = false;
    private String[] nextAnimations;

    public FBXAnimation(String animationName, long animationId, FBXCurveNode[] nodes) {
        this.animationName = animationName;
        this.animationId = animationId;
        this.nodes = nodes;
        this.isEndless = true;
    }

    public FBXAnimation(String animationName, long animationId, FBXCurveNode[] nodes, boolean isEndless) {
        this.animationName = animationName;
        this.animationId = animationId;
        this.nodes = nodes;
        this.isEndless = isEndless;
    }

    public boolean isEndless() {
        return isEndless;
    }

    public String getAnimationName() {
        return animationName;
    }

    public void setFrame(int currentFrame) {
        this.currentFrame = currentFrame - 1;
        tickFrame();
    }

    public void tickFrame() {
        for (FBXCurveNode node : nodes) {
            if (node.updateValues(this.currentFrame)) {
                if(isEndless) this.currentFrame = 0;
                else isEnd = true;
                return;
            }
        }
        this.currentFrame++;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    @Override
    protected FBXAnimation clone() {
        return new FBXAnimation(animationName, animationId, nodes.clone());
    }

    public FBXAnimation clone(boolean isEndless) {
        return new FBXAnimation(animationName, animationId, nodes.clone(), isEndless);
    }

    public long getAnimationId() {
        return animationId;
    }

    public FBXCurveNode[] getNodes() {
        return nodes;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public FBXAnimation setNextAnimations(String[] nextAnimations) {
        this.nextAnimations =nextAnimations;
        return this;
    }

    public String[] getNextAnimations() {
        return nextAnimations;
    }
}
