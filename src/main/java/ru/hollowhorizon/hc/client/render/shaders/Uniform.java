package ru.hollowhorizon.hc.client.render.shaders;

import java.util.Objects;

public final class Uniform {
    private final String name;
    private final UniformType type;

    Uniform(String name, UniformType type) {
        this.name = name;
        this.type = type;
    }

    public String name() {
        return name;
    }

    public UniformType type() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Uniform that = (Uniform) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }

    @Override
    public String toString() {
        return "Uniform[" +
                "name=" + name + ", " +
                "type=" + type + ']';
    }

}
