package ru.hollowhorizon.hc.client.render.shaders;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourceManagerReloadListener;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import ru.hollowhorizon.hc.HollowCore;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntSupplier;


public class ShaderProgram implements IResourceManagerReloadListener {

    private final List<ShaderObject> shaders;
    private final List<Uniform> uniforms;
    private final Consumer<UniformCache> cacheCallback;
    private final ShaderUniformCache uniformCache;
    private final List<String> attributes;
    private int programId = -1;
    private boolean bound;
    private final Map<String, IntSupplier> samplerMap = Maps.newHashMap();
    private final List<String> samplerNames = Lists.newArrayList();
    private final List<Integer> samplerLocations = Lists.newArrayList();

    ShaderProgram(Collection<ShaderObject> shaders, Collection<Uniform> uniforms, Consumer<UniformCache> cacheCallback, List<String> attributes) {
        this.shaders = ImmutableList.copyOf(shaders);
        this.uniforms = ImmutableList.copyOf(uniforms);
        this.cacheCallback = cacheCallback;
        this.uniformCache = new ShaderUniformCache(this);
        this.attributes = attributes;
    }

    public List<ShaderObject> getShaders() {
        return shaders;
    }

    public List<Uniform> getUniforms() {
        return uniforms;
    }

    public UniformCache getUniformCache() {
        return uniformCache;
    }

    public int getProgramId() {
        return programId;
    }

    public void use() {
        if (bound) {
            throw new IllegalStateException("Already bound.");
        }
        compile();
        GL20.glUseProgram(programId);
        cacheCallback.accept(uniformCache);
        bound = true;

        for(int i = 0; i < this.samplerLocations.size(); i++) {
            String s = this.samplerNames.get(i);
            IntSupplier intsupplier = this.samplerMap.get(s);
            if (intsupplier != null) {
                RenderSystem.activeTexture('\u84c0' + i);
                RenderSystem.enableTexture();
                int j = intsupplier.getAsInt();
                if (j != -1) {
                    RenderSystem.bindTexture(j);
                    ShaderUniform.uploadInteger(this.samplerLocations.get(i), i);
                }
            }
        }
    }

    public void setSampler(String samplerName, IntSupplier samplerValue) {
        if(!this.samplerNames.contains(samplerName)) {
            this.samplerNames.add(samplerName);
            this.updateLocations();
        }

        this.samplerMap.put(samplerName, samplerValue);
    }

    public void compile() {
        if (programId != -1 && shaders.stream().noneMatch(ShaderObject::isDirty)) return;

        for (ShaderObject shaderObject : shaders) {
            shaderObject.alloc();
        }

        if (programId == -1) {
            programId = GL20.glCreateProgram();
            if (programId == 0) {
                throw new IllegalStateException("Allocation of ShaderProgram has failed.");
            }
            shaders.forEach(shader -> GL20.glAttachShader(programId, shader.getShaderID()));
        }
        GL20.glLinkProgram(programId);

        for(int i = 0; i < this.attributes.size(); i++) {
            GL20.glBindAttribLocation(programId, i, attributes.get(i));
        }

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("ShaderProgram linkage failure. \n" + GL20.glGetProgramInfoLog(programId));
        }
        for (ShaderObject shader : shaders) {
            shader.onLink(programId);
        }
        uniformCache.onLink();

        updateLocations();
    }

    private void updateLocations() {
        this.samplerLocations.clear();
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);

        for (String s : this.samplerNames) {
            int j = ShaderUniform.glGetUniformLocation(this.programId, s);
            if (j == -1) {
                HollowCore.LOGGER.warn("Shader {} could not find sampler named {} in the specified shader program.", this.getProgramId(), s);
            } else {
                this.samplerLocations.add(j);
            }
        }

    }

    public void release() {
        if (!bound) {
            throw new IllegalStateException("Not bound");
        }
        bound = false;
        GL20.glUseProgram(0);

        for(int i = 0; i < this.samplerLocations.size(); ++i) {
            if (this.samplerMap.get(this.samplerNames.get(i)) != null) {
                GlStateManager._activeTexture('\u84c0' + i);
                GlStateManager._disableTexture();
                GlStateManager._bindTexture(0);
            }
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        for (ShaderObject shader : shaders) {
            if (shader instanceof IResourceManagerReloadListener) {
                ((IResourceManagerReloadListener) shader).onResourceManagerReload(resourceManager);
            }
        }
        compile();
    }

    public void processUniforms(Consumer<UniformCache> cacheCallback) {
        cacheCallback.accept(this.getUniformCache());
    }
}
