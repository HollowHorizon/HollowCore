package ru.hollowhorizon.hc.client.audio.decoder;

import ru.hollowhorizon.hc.HollowCore;

import java.io.EOFException;
import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.nio.file.Files;

public final class WavInputStream extends FilterInputStream {
    public int channels, sampleRate, dataRemaining;
    private final File file;

    public boolean open() {
        try {
            if (read() != 'R' || read() != 'I' || read() != 'F' || read() != 'F') {
                HollowCore.LOGGER.warn("RIFF header not found: " + file.getPath());
                return false;
            }
            skipFully(4);

            if (read() != 'W' || read() != 'A' || read() != 'V' || read() != 'E') {
                HollowCore.LOGGER.warn("Invalid wave file header: " + file.getPath());
                return false;
            }
            int fmtChunkLength = seekToChunk('f', 'm', 't', ' ');

            // http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
            // http://soundfile.sapp.org/doc/WaveFormat/
            int type = read() & 0xff | (read() & 0xff) << 8;
            if (type != 1) {
                String name;
                switch (type) {
                    case 0x0002:
                        name = "ADPCM";
                        break;
                    case 0x0003:
                        name = "IEEE float";
                        break;
                    case 0x0006:
                        name = "8-bit ITU-T G.711 A-law";
                        break;
                    case 0x0007:
                        name = "8-bit ITU-T G.711 u-law";
                        break;
                    case 0xFFFE:
                        name = "Extensible";
                        break;
                    default:
                        name = "Unknown";
                }
                HollowCore.LOGGER.warn("WAV files must be PCM, unsupported format: " + name + " (" + type + ")");
                return false;
            }

            channels = read() & 0xff | (read() & 0xff) << 8;
            if (channels != 1 && channels != 2) {
                HollowCore.LOGGER.warn("WAV files must have 1 or 2 channels: " + channels);
                return false;
            }

            sampleRate = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;

            skipFully(6);

            int bitsPerSample = read() & 0xff | (read() & 0xff) << 8;
            if (bitsPerSample != 16) {
                HollowCore.LOGGER.warn("WAV files must have 16 bits per sample: " + bitsPerSample);
                return false;
            }

            skipFully(fmtChunkLength - 16);

            dataRemaining = seekToChunk('d', 'a', 't', 'a');

            return true;
        } catch (Throwable ex) {
            HollowCore.LOGGER.warn("Error reading WAV file: " + file.getPath());
            return false;
        }
    }

    public WavInputStream(File file) throws IOException {
        super(Files.newInputStream(file.toPath()));
        this.file = file;
    }

    private int seekToChunk(char c1, char c2, char c3, char c4) throws IOException {
        while (true) {
            boolean found = read() == c1;
            found &= read() == c2;
            found &= read() == c3;
            found &= read() == c4;
            int chunkLength = read() & 0xff | (read() & 0xff) << 8 | (read() & 0xff) << 16 | (read() & 0xff) << 24;
            if (chunkLength == -1) throw new IOException("Chunk not found: " + c1 + c2 + c3 + c4);
            if (found) return chunkLength;
            skipFully(chunkLength);
        }
    }

    private void skipFully(int count) throws IOException {
        while (count > 0) {
            long skipped = in.skip(count);
            if (skipped <= 0) throw new EOFException("Unable to skip.");
            count -= skipped;
        }
    }

    public int read(byte[] buffer) throws IOException {
        if (dataRemaining == 0) return -1;
        int offset = 0;
        do {
            int length = Math.min(super.read(buffer, offset, buffer.length - offset), dataRemaining);
            if (length == -1) {
                if (offset > 0) return offset;
                return -1;
            }
            offset += length;
            dataRemaining -= length;
        } while (offset < buffer.length);
        return offset;
    }
}
