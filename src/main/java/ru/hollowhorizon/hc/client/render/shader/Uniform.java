package ru.hollowhorizon.hc.client.render.shader;

public class Uniform {

    private final String name;
    private final UniformType type;

    public Uniform(String name, UniformType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public UniformType getType() {
        return type;
    }
}
