package ru.hollowhorizon.hc.client.model.fbx;

public class FBXAnimation implements Cloneable {
    private final String animationName;
    private final long animationId;
    private final FBXCurveNode[] nodes;
    private int currentFrame;

    public FBXAnimation(String animationName, long animationId, FBXCurveNode[] nodes) {
        this.animationName = animationName;
        this.animationId = animationId;
        this.nodes = nodes;
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
                this.currentFrame = 0;
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

    public long getAnimationId() {
        return animationId;
    }

    public FBXCurveNode[] getNodes() {
        return nodes;
    }

}
