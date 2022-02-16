package ru.hollowhorizon.hc.client.model.fbx.raw;

import java.io.IOException;

public class FBXProperty<T> {
    private final char character;
    private final T data;

    public FBXProperty(char character, T data) {
        this.character = character;
        this.data = data;

    }

    public static FBXProperty<?> load(HollowByteStream stream, char character) throws IOException {
        switch (character) {
            case 'Y':
                return new FBXProperty<>(character, stream.readInt());
            case 'C':
                return new FBXProperty<>(character, stream.readByte() == 1);
            case 'I':
                return new FBXProperty<>(character, stream.readUInt());
                case 'F':
                return new FBXProperty<>(character, stream.readFloat());
            case 'D':
                return new FBXProperty<>(character, stream.readDouble());
            case 'L':
                return new FBXProperty<>(character, stream.readLong());
            case 'S':
            case 'R':
                return new FBXProperty<>(character, stream.readBigString());
            case 'f':
                return new FBXProperty<>(character, stream.readFloatArray());
            case 'i':
                return new FBXProperty<>(character, stream.readIntArray());
            case 'd':
                return new FBXProperty<>(character, stream.readDoubleArray());
            case 'l':
                return new FBXProperty<>(character, stream.readLongArray());
            case 'b':
                return new FBXProperty<>(character, stream.readBoolArray());
            case 'c':
                return new FBXProperty<>(character, stream.readRawArray(1));
        }
        return new FBXProperty<>(character, null);
    }

    @Override
    public String toString() {
        return "[" + character + ", " + data + ']';
    }

    public char getCharacter() {
        return character;
    }

    @SuppressWarnings("unchecked")
    public <R, K extends R> K getData() {
        return (K) data;
    }
}
