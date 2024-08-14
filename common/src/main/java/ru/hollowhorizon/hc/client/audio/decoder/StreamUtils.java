package ru.hollowhorizon.hc.client.audio.decoder;

import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public final class StreamUtils {
    public static final int DEFAULT_BUFFER_SIZE = 4096;
    public static final byte[] EMPTY_BYTES = new byte[0];

    public static void copyStream(InputStream input, OutputStream output) throws IOException {
        copyStream(input, output, new byte[DEFAULT_BUFFER_SIZE]);
    }

    public static void copyStream(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
    }

    public static byte[] copyStreamToByteArray (InputStream input, int estimatedSize) throws IOException {
        ByteArrayOutputStream baos = new OptimizedByteArrayOutputStream(Math.max(0, estimatedSize));
        copyStream(input, baos);
        return baos.toByteArray();
    }

    /** Close and ignore all errors. */
    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable ignored) {
            }
        }
    }

    /** A ByteArrayOutputStream which avoids copying of the byte array if possible. */
    static public class OptimizedByteArrayOutputStream extends ByteArrayOutputStream {
        public OptimizedByteArrayOutputStream (int initialSize) {
            super(initialSize);
        }

        @Override
        public synchronized byte[] toByteArray () {
            if (count == buf.length) return buf;
            return super.toByteArray();
        }

        public byte[] getBuffer () {
            return buf;
        }
    }
}
