package ru.hollowhorizon.hc.client.models.core.materials;

import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.client.shader.ShaderLoader;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.models.core.BoneTownRegistry;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.OptionalInt;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class MaterialResourceManager implements ISelectiveResourceReloadListener {

    public static final MaterialResourceManager INSTANCE = new MaterialResourceManager();

    private IResourceManager manager;
    private HashMap<ResourceLocation, IBTMaterial> programCache = new HashMap<>();


    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        if(resourcePredicate.test(getResourceType())) {
            clearProgramCache();
            onShadersReload(resourceManager);
        }
    }

    private void clearProgramCache() {
        programCache.values().forEach(ShaderLinkHelper::releaseProgram);
        programCache.clear();
    }

    public OptionalInt getProgramId(ResourceLocation location) {
        IBTMaterial prog = programCache.get(location);
        return prog == null ? OptionalInt.empty() : OptionalInt.of(prog.getId());
    }


    public IBTMaterial getShaderProgram(ResourceLocation location) {
        return programCache.get(location);
    }


    public void onShadersReload(IResourceManager resourceManager) {
        this.manager = resourceManager;
        for (BTMaterialEntry entry : BoneTownRegistry.MATERIAL_REGISTRY.getValues()) {
            HollowCore.LOGGER.info("Loading program: {}", entry.getRegistryName().toString());
            loadProgram(entry);
        }
    }

    private void loadProgram(BTMaterialEntry program) {
        try {
            ShaderLoader vert = createShader(manager, program.getVertexShader(), ShaderLoader.ShaderType.VERTEX);
            ShaderLoader frag = createShader(manager, program.getFragShader(), ShaderLoader.ShaderType.FRAGMENT);
            int progId = ShaderLinkHelper.createProgram();
            IBTMaterial prog = program.getProgram(progId, vert, frag);
            ShaderLinkHelper.linkProgram(prog);
            prog.setupUniforms();
            programCache.put(program.getRegistryName(), prog);
        } catch (IOException ex) {
            HollowCore.LOGGER.error("Failed to load program {}",
                    program.getRegistryName().toString(), ex);
        }
    }

    @Nullable
    @Override
    public IResourceType getResourceType() {
        return VanillaResourceType.SHADERS;
    }

    private static ShaderLoader createShader(IResourceManager manager, ResourceLocation location,
                                             ShaderLoader.ShaderType shaderType) throws IOException {
        HollowCore.LOGGER.info("Trying to create shader: {}", location.toString());
        IResource res = manager.getResource(location);
        try (InputStream is = new BufferedInputStream(res.getInputStream())) {
            return ShaderLoader.compileShader(shaderType, location.toString(), is, res.getSourceName());
        }
    }
}
