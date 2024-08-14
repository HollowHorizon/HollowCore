package ru.hollowhorizon.hc.client.audio.decoder;

public interface FrameDecoder {
    void decodeFrame() throws DecoderException;
}
