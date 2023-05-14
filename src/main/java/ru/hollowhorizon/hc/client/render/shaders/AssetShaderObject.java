package ru.hollowhorizon.hc.client.render.shaders;

import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AssetShaderObject extends AbstractShaderObject implements IResourceManagerReloadListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation asset;
    private String source;

    public AssetShaderObject(String name, ShaderType type, Collection<Uniform> uniforms, ResourceLocation asset) {
        super(name, type, uniforms);
        this.asset = Objects.requireNonNull(asset);
    }

    @Override
    protected String getSource() {
        if (source == null) {
            source = new GlslProcessor(asset).process();
        }

        return source;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        source = null;
        dirty = true;
    }

}
