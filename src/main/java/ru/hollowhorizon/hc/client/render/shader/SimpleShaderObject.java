package ru.hollowhorizon.hc.client.render.shader;

import java.util.Collection;

public class SimpleShaderObject extends AbstractShaderObject {

    private final String source;

    protected SimpleShaderObject(String name, ShaderType type, Collection<Uniform> uniforms, String source) {
        super(name, type, uniforms);
        this.source = source;
    }

    @Override
    protected String getSource() {
        return source;
    }
}