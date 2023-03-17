package ru.hollowhorizon.hc.client.models.fbx.raw;

import java.io.*;
import java.nio.*;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class HollowByteStream {
    private final ByteArrayInputStream bytes;
    private final int size;

    public HollowByteStream(InputStream stream) throws IOException {
        //преобразование входного потока в массив байтов
        byte[] bytes = new byte[stream.available()];
        DataInputStream dis = new DataInputStream(stream);
        dis.readFully(bytes);
        this.bytes = new ByteArrayInputStream(bytes);
        this.size = this.bytes.available();
    }

    public HollowByteStream(byte[] bytes) {
        this.bytes = new ByteArrayInputStream(bytes);
        this.size = bytes.length;
    }

    //считать строку с заданным размером (1 байт)
    public String readString() throws IOException {
        byte len = readByte();
        return new String(read(len));
    }

    //считать строку с заданным размером (4 байта)
    public String readBigString() throws IOException {
        int len = readUInt();
        return new String(read(len));
    }

    //считать целое число (4 байта)
    public int readUInt() throws IOException {
        return ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    //считать байт
    public byte readByte() throws IOException {
        return read(1)[0];
    }

    //считать символ (1 байт)
    public char readChar() throws IOException {
        return (char) read(1)[0];
    }

    //получить массив байтов указанной длины
    public byte[] read(int length) throws IOException {
        byte[] data = new byte[length];
        bytes.read(data);
        return data;
    }

    //считать целое число (2 байта)
    public int readInt() throws IOException {
        return ByteBuffer.wrap(read(2)).getInt();
    }

    //считать число с плавающей точкой (4 байта)
    public float readFloat() throws IOException {
        return ByteBuffer.wrap(read(4)).getFloat();
    }

    //считать число с плавающей точкой (8 байт)
    public double readDouble() throws IOException {
        return ByteBuffer.wrap(read(8)).getDouble();
    }

    //считать число с двойной точностью (8 байт)
    public long readLong() throws IOException {
        return ByteBuffer.wrap(read(8)).getLong();
    }

    //считать массив целых чисел (4 байта)
    public int[] readIntArray() throws IOException {
        IntBuffer buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer();
        int[] array = new int[buffer.remaining()];
        buffer.get(array);
        return array;

    }

    //считать массив чисел с двойной точностью (8 байт)
    public long[] readLongArray() throws IOException {
        LongBuffer buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asLongBuffer();
        long[] array = new long[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    //считать массив логических значений (1 байт)
    public boolean[] readBoolArray() throws IOException {
        byte[] binaryData = readRawArray(1);
        boolean[] result = new boolean[binaryData.length];
        for (int i = 0; i < binaryData.length; i++) {
            result[i] = (binaryData[i] != 0);
        }
        return result;
    }

    //считать массив чисел с плавающей точкой (4 байта)
    public float[] readFloatArray() throws IOException {
        FloatBuffer buffer = ByteBuffer.wrap(readRawArray(4)).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        float[] array = new float[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    //считать массив чисел с плавающей точкой (8 байт)
    public double[] readDoubleArray() throws IOException {
        DoubleBuffer buffer = ByteBuffer.wrap(readRawArray(8)).order(ByteOrder.LITTLE_ENDIAN).asDoubleBuffer();
        double[] array = new double[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    //считать массив байтов
    public byte[] readRawArray(int dataSize) throws IOException {
        int length = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int encoding = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        int compressedSize = ByteBuffer.wrap(read(4)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        byte[] arrayData;
        if (encoding == 1) {
            arrayData = decompressData(read(compressedSize));
        } else {
            arrayData = read(length * dataSize);
        }
        return arrayData;
    }

    //декомпрессия данных
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
