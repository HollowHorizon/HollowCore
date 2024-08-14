package ru.hollowhorizon.hc.client.audio.decoder;

public class OutputChannels {

    public static final int BOTH_CHANNELS = 0;
    public static final int LEFT_CHANNEL = 1;
    public static final int RIGHT_CHANNEL = 2;
    public static final int DOWNMIX_CHANNELS = 3;

    public static final OutputChannels LEFT = new OutputChannels(LEFT_CHANNEL);
    public static final OutputChannels RIGHT = new OutputChannels(RIGHT_CHANNEL);
    public static final OutputChannels BOTH = new OutputChannels(BOTH_CHANNELS);
    public static final OutputChannels DOWNMIX = new OutputChannels(DOWNMIX_CHANNELS);

    private final int outputChannels;

    static public OutputChannels fromInt (int code) {
        switch (code) {
            case LEFT_CHANNEL:
                return LEFT;
            case RIGHT_CHANNEL:
                return RIGHT;
            case BOTH_CHANNELS:
                return BOTH;
            case DOWNMIX_CHANNELS:
                return DOWNMIX;
            default:
                throw new IllegalArgumentException("Invalid channel code: " + code);
        }
    }

    private OutputChannels (int channels) {
        outputChannels = channels;

        if (channels < 0 || channels > 3) throw new IllegalArgumentException("channels");
    }

    public int getChannelsOutputCode () {
        return outputChannels;
    }

    public int getChannelCount () {
        int count = outputChannels == BOTH_CHANNELS ? 2 : 1;
        return count;
    }

    public boolean equals (Object o) {
        boolean equals = false;

        if (o instanceof OutputChannels) {
            OutputChannels oc = (OutputChannels)o;
            equals = oc.outputChannels == outputChannels;
        }

        return equals;
    }

    public int hashCode () {
        return outputChannels;
    }

}
