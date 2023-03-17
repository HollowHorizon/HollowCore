package ru.hollowhorizon.hc.client.models.core.bonemf;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class BoneMFAnimation {
    private final String name;

    private final Map<String, BoneMFAnimationChannel> channels;

    private long frameCount;
    private double frameRate;

    public BoneMFAnimation(String name){
        this.name = name;
        channels = new HashMap<>();
    }

    public void addChannel(String channelName, BoneMFAnimationChannel channel){
        channels.put(channelName, channel);
    }

    public String getName() {
        return name;
    }

    @Nullable
    public BoneMFAnimationChannel getChannel(String name){
        return channels.get(name);
    }

    public Map<String, BoneMFAnimationChannel> getChannels() {
        return channels;
    }

    public void setFrameCount(long frameCount) {
        this.frameCount = frameCount;
    }

    public void setFrameRate(double frameRate) {
        this.frameRate = frameRate;
    }

    public double getFrameRate() {
        return frameRate;
    }

    public long getFrameCount() {
        return frameCount;
    }
}
