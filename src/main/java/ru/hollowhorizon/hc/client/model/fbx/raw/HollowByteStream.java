package ru.hollowhorizon.hc.client.model.fbx.raw;

import com.google.common.primitives.Bytes;
import sun.misc.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class HollowByteStream {
    private final ByteArrayInputStream bytes;
    private final int size;

    public HollowByteStream(InputStream stream) throws IOException {
        this.bytes = new ByteArrayInputStream(IOUtils.readAllBytes(stream));
        this.size = bytes.available();
    }

    public HollowByteStream(byte[] bytes) {
        this.bytes = new ByteArrayInputStream(bytes);
        this.size = bytes.length;
    }

    public String readString() throws IOException {
        byte len = readByte();
        return new String(read(len));
    }

    public String readBigString() throws IOException {
        int len = readUInt();
        return new String(read(len));
    }

    public int readUInt() throws IOException {
        return ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public byte readByte() throws IOException {
        return read(1)[0];
    }

    public char readChar() throws IOException {
        return (char) read(1)[0];
    }

    public byte[] read(int length) throws IOException {
        byte[] data = new byte[length];
        bytes.read(data);
        return data;
    }

    public int readInt() throws IOException {
        return ByteBuffer.wrap(read(2)).getInt();
    }

    public float readFloat() throws IOException {
        return ByteBuffer.wrap(read(4)).getFloat();
    }

    public double readDouble() throws IOException {
        return ByteBuffer.wrap(read(8)).getDouble();
    }

    public long readLong() throws IOException {
        return ByteBuffer.wrap(read(8)).getLong();
    }

    public int[] readIntArray() throws IOException {
        IntBuffer buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] array = new int[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    public long[] readLongArray() throws IOException {
        LongBuffer buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
        long[] array = new long[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    public boolean[] readBoolArray() throws IOException {
        byte[] binaryData = readRawArray(1);
        boolean[] result = new boolean[binaryData.length];
        for (int i = 0; i < binaryData.length; i++) {
            result[i] = (binaryData[i] != 0);
        }
        return result;
    }

    public float[] readFloatArray() throws IOException {
        FloatBuffer buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] array = new float[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    public double[] readDoubleArray() throws IOException {
        DoubleBuffer buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
        double[] array = new double[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    public byte[] readRawArray(int dataSize) throws IOException {
        int length = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int encoding = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int compressedSize = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        byte[] arrayData;
        if(encoding == 1) {
            arrayData = decompressData(read(compressedSize));
        }else {
            arrayData = read(length*dataSize);
        }
        return arrayData;
    }

    private byte[] decompressData(byte[] compressedData) {

        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        byte[] buffer = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buffer);
                outputStream.write(buffer, 0, count);
            } catch (DataFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        decompressor.end();
        return outputStream.toByteArray();

    }

    public int available() {
        return size - bytes.available();
    }
}
