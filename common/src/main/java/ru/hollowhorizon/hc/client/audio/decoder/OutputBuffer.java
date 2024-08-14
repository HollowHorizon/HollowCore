package ru.hollowhorizon.hc.client.audio.decoder;

public final class OutputBuffer {
    public static final int BUFFERSIZE = 2 * 1152; // max. 2 * 1152 samples per frame
    private static final int MAXCHANNELS = 2; // max. number of channels

    private Float replayGainScale;
    private final int channels;
    private final byte[] buffer;
    private final int[] channelPointer;
    private final boolean isBigEndian;

    public OutputBuffer (int channels, boolean isBigEndian) {
        this.channels = channels;
        this.isBigEndian = isBigEndian;
        buffer = new byte[BUFFERSIZE * channels];
        channelPointer = new int[channels];
        reset();
    }

    private void append (int channel, short value) {
        byte firstByte;
        byte secondByte;
        if (isBigEndian) {
            firstByte = (byte)(value >>> 8 & 0xFF);
            secondByte = (byte)(value & 0xFF);
        } else {
            firstByte = (byte)(value & 0xFF);
            secondByte = (byte)(value >>> 8 & 0xFF);
        }
        buffer[channelPointer[channel]] = firstByte;
        buffer[channelPointer[channel] + 1] = secondByte;
        channelPointer[channel] += channels * 2;
    }

    public void appendSamples (int channel, float[] f) {
        short s;
        if (replayGainScale != null) {
            for (int i = 0; i < 32;) {
                s = clip(f[i++] * replayGainScale);
                append(channel, s);
            }
        } else {
            for (int i = 0; i < 32;) {
                s = clip(f[i++]);
                append(channel, s);
            }
        }
    }

    public byte[] getBuffer () {
        return buffer;
    }

    public int reset () {
        try {
            int index = channels - 1;
            return channelPointer[index] - index * 2;
        } finally {
            // Points to byte location, implicitely assuming 16 bit samples.
            for (int i = 0; i < channels; i++)
                channelPointer[i] = i * 2;
        }
    }

    public void setReplayGainScale (Float replayGainScale) {
        this.replayGainScale = replayGainScale;
    }

    public boolean isStereo () {
        return channelPointer[1] == 2;
    }

    private short clip (float sample) {
        return sample > 32767.0f ? 32767 : sample < -32768.0f ? -32768 : (short)sample;
    }

}
