package ru.hollowhorizon.hc.client.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import ru.hollowhorizon.hc.HollowCore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * A ShaderProgram.
 * You probably want {@link ShaderProgramBuilder} to construct a ShaderProgram.
 * it should be noted, that a ShaderProgram is a {@link ISelectiveResourceReloadListener},
 * its recommended that you ensure this is registered to {@link IReloadableResourceManager}
 * to ensure {@link ShaderObject}s are re loaded properly when Resources are relaoded.
 * <p>
 * Created by covers1624 on 24/5/20.
 */
public class ShaderProgram implements ISelectiveResourceReloadListener {

    private final List<ShaderObject> shaders;
    private final Consumer<UniformCache> cacheCallback;
    private final ShaderUniformCache uniformCache;
    private int programId = -1;
    private boolean bound;

    public ShaderProgram(Collection<ShaderObject> shaders) {
        this(shaders, e -> {
        });
    }

    public ShaderProgram(Collection<ShaderObject> shaders, Consumer<UniformCache> cacheCallback) {
        this.shaders = new ArrayList<>(shaders);
        this.cacheCallback = cacheCallback;
        this.uniformCache = new ShaderUniformCache(this);
    }

    /**
     * Gets all {@link ShaderObject}s that make up this {@link ShaderProgram}.
     *
     * @return The {@link ShaderObject}s.
     */
    public List<ShaderObject> getShaders() {
        return Collections.unmodifiableList(shaders);
    }

    /**
     * Gets the GL {@link ShaderProgram} id for this shader.
     * Might not be initialized until {@link #use()} is called once.
     *
     * @return The id, -1 if not initialized.
     */
    public int getProgramId() {
        return programId;
    }

    /**
     * Allocates a new {@link UniformCache} for this {@link ShaderProgram},
     * Must be returned with {@link #popCache} to have uniform changes applied.
     *
     * @return The {@link UniformCache}.
     */
    public UniformCache pushCache() {
        UniformCache cache = uniformCache.pushCache();
        cacheCallback.accept(cache);
        return cache;
    }

    public void use() {
        if (bound) {
            //throw new IllegalStateException("Already bound.");
        }
        if (programId == -1 || shaders.stream().anyMatch(ShaderObject::isDirty)) {
            shaders.forEach(ShaderObject::alloc);
            if (programId == -1) {
                programId = GL20.glCreateProgram();
                if (programId == 0) {
                    throw new IllegalStateException("Allocation of ShaderProgram has failed.");
                }
                shaders.forEach(shader -> GL20.glAttachShader(programId, shader.getShaderID()));
            }
            GL20.glLinkProgram(programId);
            if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
                throw new RuntimeException("ShaderProgram linkage failure. \n" + GL20.glGetProgramInfoLog(programId));
            }
            shaders.forEach(shader -> shader.onLink(programId));
            uniformCache.onLink();
        }
        GL20.glUseProgram(programId);
        bound = true;
    }

    public void use(IUniforms uniforms, IUsable render) {
        UniformCache cache = uniforms.onUse(this.pushCache());

        this.use();

        RenderSystem.depthFunc(519);

        render.onUse();

        RenderSystem.depthFunc(515);

        this.popCache(cache);

        this.release();
    }

    public void popCache(UniformCache cache) {
        if (!bound) {
            throw new IllegalStateException("Not bound");
        }
        uniformCache.popApply((ShaderUniformCache) cache);
    }

    /**
     * Releases this shader.
     */
    public void release() {
        if (!bound) {
            return;
        }
        bound = false;
        GL20.glUseProgram(0);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        for (ShaderObject shader : shaders) {
            if (shader instanceof ISelectiveResourceReloadListener) {
                ((ISelectiveResourceReloadListener) shader).onResourceManagerReload(resourceManager, resourcePredicate);
            }
        }
    }

    public interface IUniforms {
        UniformCache onUse(UniformCache cache);
    }

    public interface IUsable {
        void onUse();
    }
}
