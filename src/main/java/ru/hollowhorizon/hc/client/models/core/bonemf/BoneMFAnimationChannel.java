package ru.hollowhorizon.hc.client.models.core.bonemf;

import java.util.ArrayList;
import java.util.List;

public class BoneMFAnimationChannel {

    private final List<BoneMFNodeFrame> frames;
    private final String nodeName;

    public BoneMFAnimationChannel(String nodeName){
        frames = new ArrayList<>();
        this.nodeName = nodeName;
    }

    public void addNodeFrame(BoneMFNodeFrame nodeFrame){
        this.frames.add(nodeFrame);
    }

    public List<BoneMFNodeFrame> getFrames() {
        return frames;
    }

    public String getNodeName() {
        return nodeName;
    }
}
