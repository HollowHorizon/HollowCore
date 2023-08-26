package ru.hollowhorizon.hc.client.render.shaders;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;

public abstract class NamedShaderObject implements ShaderObject {

    private final String name;
    private final ShaderType type;
    private final ImmutableList<Uniform> uniforms;

    protected NamedShaderObject(String name, ShaderType type, Collection<Uniform> uniforms) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.uniforms = ImmutableList.copyOf(uniforms);
    }

    @Nonnull
    @Override
    public String getShaderName() {
        return name;
    }

    @Override
    public ShaderType getShaderType() {
        return type;
    }

    @Override
    public ImmutableList<Uniform> getUniforms() {
        return uniforms;
    }
}
