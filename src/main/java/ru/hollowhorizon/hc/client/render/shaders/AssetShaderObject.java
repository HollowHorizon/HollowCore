package ru.hollowhorizon.hc.client.render.shaders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class AssetShaderObject extends AbstractShaderObject implements ResourceManagerReloadListener {

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
    public void onResourceManagerReload(ResourceManager resourceManager) {
        source = null;
        dirty = true;
    }

}
