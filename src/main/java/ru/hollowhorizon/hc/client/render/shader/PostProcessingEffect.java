package ru.hollowhorizon.hc.client.render.shader;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;
import ru.hollowhorizon.hc.client.render.HollowRenderHelper;

import java.io.StringWriter;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public abstract class PostProcessingEffect<T extends PostProcessingEffect<?>> {
    //Clear colors
    private float cr, cg, cb, ca;

    //Additional stages
    private PostProcessingEffect<?>[] stages;

    //Buffers
    private static final FloatBuffer TEXEL_SIZE_BUFFER = GLAllocation.createFloatBuffer(2);
    private static final FloatBuffer CLEAR_COLOR_BUFFER = GLAllocation.createFloatBuffer(16);
    private static final FloatBuffer MATRIX4F_BUFFER = GLAllocation.createFloatBuffer(16);
    private static final FloatBuffer FLOAT_BUFFER_1 = GLAllocation.createFloatBuffer(1);
    private static final FloatBuffer FLOAT_BUFFER_2 = GLAllocation.createFloatBuffer(2);
    private static final FloatBuffer FLOAT_BUFFER_3 = GLAllocation.createFloatBuffer(3);
    private static final FloatBuffer FLOAT_BUFFER_4 = GLAllocation.createFloatBuffer(4);
    private static final IntBuffer INT_BUFFER_1 = HollowRenderHelper.createDirectIntBuffer(1);
    private static final IntBuffer INT_BUFFER_2 = HollowRenderHelper.createDirectIntBuffer(2);
    private static final IntBuffer INT_BUFFER_3 = HollowRenderHelper.createDirectIntBuffer(3);
    private static final IntBuffer INT_BUFFER_4 = HollowRenderHelper.createDirectIntBuffer(4);

    private int shaderProgramID = -1;
    private int diffuseSamplerUniformID = -1;
    private int texelSizeUniformID = -1;

    private boolean initialized = false;

    /**
     * Initializes the effect. Requires an OpenGL context to work
     */
    @SuppressWarnings("unchecked")
    public final T init() {
        //Only initialize once to prevent allocation memory leaks
        if(!this.initialized) {
            this.initShaders();
            this.stages = this.getStages();
            if(this.stages != null && this.stages.length > 0) {
                for(PostProcessingEffect<?> stage : this.stages) {
                    stage.init();
                }
            }
            if(!this.initEffect() || this.shaderProgramID < 0) {
                throw new RuntimeException("Couldn't initialize shaders for post processing effect: " + this.toString());
            }
            this.initialized = true;
        }
        return (T) this;
    }

    /**
     * Sets the default background color
     * @param r Red
     * @param g Green
     * @param b Blue
     * @param a Alpha
     * @return
     */
    @SuppressWarnings("unchecked")
    public final T setBackgroundColor(float r, float g, float b, float a) {
        this.cr = r;
        this.cg = g;
        this.cb = b;
        this.ca = a;
        return (T) this;
    }

    /**
     * Returns the shader program ID
     */
    public final int getShaderProgram() {
        return this.shaderProgramID;
    }

    /**
     * Deletes all shaders and frees memory
     */
    public final void delete() {
        if(this.shaderProgramID > 0)
            GL20.glDeleteProgram(this.shaderProgramID);
        if(this.stages != null && this.stages.length > 0) {
            for(PostProcessingEffect<?> stage : this.stages) {
                stage.delete();
            }
        }
        this.deleteEffect();
    }

    /**
     * Returns the effect builder
     * @param dst
     * @return
     */
    public EffectBuilder<T> create(Framebuffer dst) {
        return new EffectBuilder<T>(this, dst);
    }

    public static final class EffectBuilder<T extends PostProcessingEffect<?>> {
        private final PostProcessingEffect<T> effect;
        private final Framebuffer dst;

        private int src = -1;
        private Framebuffer blitFrfamebuffer = null;
        private Framebuffer prevFramebuffer = null;
        private double renderWidth = -1.0D;
        private double renderHeight = -1.0D;
        private boolean restore = true;
        private boolean mirrorX = false;
        private boolean mirrorY = false;
        private boolean clearDepth = true;
        private boolean clearColor = true;

        private EffectBuilder(PostProcessingEffect<T> effect, Framebuffer dst) {
            this.effect = effect;
            this.dst = dst;
        }

        /**
         * Sets the source texture the effect should read from.
         * <p><b>Note:</b> If the source is the same as the destination a blit buffer is required
         * @param src
         * @return
         */
        public EffectBuilder<T> setSource(int src) {
            this.src = src;
            return this;
        }

        /**
         * Sets the blit FBO if this effect requires one
         * @param blitFramebuffer
         * @return
         */
        public EffectBuilder<T> setBlitFramebuffer(Framebuffer blitFramebuffer) {
            this.blitFrfamebuffer = blitFramebuffer;
            return this;
        }

        /**
         * Sets which FBO should be bound after applying the effect
         * @param prevFramebuffer
         * @return
         */
        public EffectBuilder<T> setPreviousFramebuffer(Framebuffer prevFramebuffer) {
            this.prevFramebuffer = prevFramebuffer;
            return this;
        }

        /**
         * Sets the render dimensions for this effect.
         * Uses the destination FBO dimensions by default
         * @param renderWidth
         * @param renderHeight
         * @return
         */
        public EffectBuilder<T> setRenderDimensions(double renderWidth, double renderHeight) {
            this.renderWidth = renderWidth;
            this.renderHeight = renderHeight;
            return this;
        }

        /**
         * Sets whether the GL states should be restored after applying the effect.
         * True by default
         * @param restore
         * @return
         */
        public EffectBuilder<T> setRestoreGlState(boolean restore) {
            this.restore = restore;
            return this;
        }

        /**
         * Sets the renderer to mirror the X axis
         * @param mirror
         * @return
         */
        public EffectBuilder<T> setMirrorX(boolean mirror) {
            this.mirrorX = mirror;
            return this;
        }

        /**
         * Sets the renderer to mirror the Y axis
         * @param mirror
         * @return
         */
        public EffectBuilder<T> setMirrorY(boolean mirror) {
            this.mirrorY = mirror;
            return this;
        }

        /**
         * Sets whether the destination FBO depth buffer should be cleared
         * @param clearDepth
         * @return
         */
        public EffectBuilder<T> setClearDepth(boolean clearDepth) {
            this.clearDepth = clearDepth;
            return this;
        }
        
        public EffectBuilder<T> setClearColor(boolean clearColor) {
            this.clearColor = clearColor;
            return this;
        }

        public void render(float partialTicks) {
            double renderWidth = this.renderWidth;
            double renderHeight = this.renderHeight;
            if(renderWidth < 0.0D || renderHeight < 0.0D) {
                renderWidth = this.dst.width;
                renderHeight = this.dst.height;
            }
            this.effect.render(partialTicks, this.src, this.dst, this.blitFrfamebuffer, this.prevFramebuffer, renderWidth, renderHeight, this.restore, this.mirrorX, this.mirrorY, this.clearDepth, this.clearColor);
        }
    }

    private void render(float partialTicks, int src, Framebuffer dst, Framebuffer blitBuffer, Framebuffer prev, double renderWidth, double renderHeight, boolean restore,
                        boolean mirrorX, boolean mirrorY, boolean clearDepth, boolean clearColor) {
        if(this.shaderProgramID == -1 || dst == null) return;

        Framebuffer intermediateDst = dst;

        //If source is the same as the destination then use a blit buffer
        if(src == dst.getColorTextureId()) {
            intermediateDst = blitBuffer;
        }

        //Bind destination FBO
        intermediateDst.bindWrite(true);

        int prevShaderProgram = 0;

        if(restore) {
            //Backup attributes
            GL11.glPushAttrib(
                    GL11.GL_MATRIX_MODE |
                            GL11.GL_VIEWPORT_BIT |
                            GL11.GL_TRANSFORM_BIT
            );
            prevShaderProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
            GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, CLEAR_COLOR_BUFFER);

            //Backup matrices
            RenderSystem.pushMatrix();
            RenderSystem.matrixMode(GL11.GL_PROJECTION);
            RenderSystem.pushMatrix();
            RenderSystem.matrixMode(GL11.GL_MODELVIEW);
            RenderSystem.pushMatrix();
        }

        //Set up 2D matrices
        RenderSystem.matrixMode(GL11.GL_PROJECTION);
        RenderSystem.loadIdentity();
        RenderSystem.ortho(0.0D, intermediateDst.width, intermediateDst.height, 0.0D, 1000.0D, 3000.0D);
        RenderSystem.matrixMode(GL11.GL_MODELVIEW);
        RenderSystem.loadIdentity();
        RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

        //Clear buffers
        if(clearDepth) {
            RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        }
        if(clearColor) {
            RenderSystem.clearColor(this.cr, this.cg, this.cb, this.ca);
            RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
        }

        //Use shader
        GL20.glUseProgram(this.shaderProgramID);

        //Upload sampler uniform (src texture ID)
        if(this.diffuseSamplerUniformID >= 0 && src >= 0) {
            this.uploadSampler(this.diffuseSamplerUniformID, src, 0);
        }

        //Upload texel size uniform
        if(this.texelSizeUniformID >= 0) {
            TEXEL_SIZE_BUFFER.position(0);
            TEXEL_SIZE_BUFFER.put(1.0F / (float)intermediateDst.width);
            TEXEL_SIZE_BUFFER.put(1.0F / (float)intermediateDst.height);
            TEXEL_SIZE_BUFFER.flip();
            GL20.glUniform1fv(this.texelSizeUniformID, TEXEL_SIZE_BUFFER);
        }

        //Uploads additional uniforms
        this.uploadUniforms(partialTicks);

        //Render texture
        RenderSystem.enableTexture();
        BufferBuilder builder = Tessellator.getInstance().getBuilder();
        builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
        builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
        builder.vertex(0, (float)renderHeight, 0).uv(mirrorX ? 1 : 0, mirrorY ? 0 : 1).endVertex();
        builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
        builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
        builder.vertex((float)renderWidth, 0, 0).uv(mirrorX ? 0 : 1, mirrorY ? 1 : 0).endVertex();
        builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
        builder.end();
        WorldVertexBufferUploader.end(builder);

        //Apply additional stages
        if(blitBuffer != null && this.stages != null && this.stages.length > 0) {
            for(PostProcessingEffect<?> stage : this.stages) {
                //Render to blit buffer
                stage.render(partialTicks, intermediateDst.getColorTextureId(), blitBuffer, intermediateDst, null, renderWidth, renderHeight, false, false, false, clearDepth, clearColor);

                //Render from blit buffer to destination buffer
                intermediateDst.bindWrite(true);
                RenderSystem.enableTexture();
                builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
                builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
                builder.vertex(0, (float)renderHeight, 0).uv(mirrorX ? 1 : 0, mirrorY ? 0 : 1).endVertex();
                builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
                builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
                builder.vertex((float)renderWidth, 0, 0).uv(mirrorX ? 0 : 1, mirrorY ? 1 : 0).endVertex();
                builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
                builder.end();
                WorldVertexBufferUploader.end(builder);
            }
        }

        //Don't use any shader to copy from blit buffer to destination
        GL20.glUseProgram(0);

        if(src == dst.getColorTextureId()) {
            //Set up 2D matrices
            RenderSystem.matrixMode(GL11.GL_PROJECTION);
            RenderSystem.loadIdentity();
            RenderSystem.ortho(0.0D, dst.width, dst.height, 0.0D, 1000.0D, 3000.0D);
            RenderSystem.matrixMode(GL11.GL_MODELVIEW);
            RenderSystem.loadIdentity();
            RenderSystem.translatef(0.0F, 0.0F, -2000.0F);

            dst.bindWrite(true);
            RenderSystem.enableTexture();
            RenderSystem.bindTexture(intermediateDst.getColorTextureId());
            builder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX);
            builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
            builder.vertex(0, (float)renderHeight, 0).uv(mirrorX ? 1 : 0, mirrorY ? 0 : 1).endVertex();
            builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
            builder.vertex((float)renderWidth, (float)renderHeight, 0).uv(mirrorX ? 0 : 1, mirrorY ? 0 : 1).endVertex();
            builder.vertex((float)renderWidth, 0, 0).uv(mirrorX ? 0 : 1, mirrorY ? 1 : 0).endVertex();
            builder.vertex(0, 0, 0).uv(mirrorX ? 1 : 0, mirrorY ? 1 : 0).endVertex();
            builder.end();
            WorldVertexBufferUploader.end(builder);
        }

        //Bind previous shader
        GL20.glUseProgram(prevShaderProgram);

        this.postRender(partialTicks);

        if(restore) {
            //Restore matrices
            RenderSystem.matrixMode(GL11.GL_PROJECTION);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(GL11.GL_MODELVIEW);
            RenderSystem.popMatrix();

            //Restore attributes
            RenderSystem.popAttributes();
            RenderSystem.clearColor(CLEAR_COLOR_BUFFER.get(0), CLEAR_COLOR_BUFFER.get(1), CLEAR_COLOR_BUFFER.get(2), CLEAR_COLOR_BUFFER.get(3));

            //Restore matrices
            RenderSystem.popMatrix();

            //Bind previous FBO
            if(prev != null)
                prev.bindWrite(true);
        }
    }

    /**
     * Initializes the shaders
     */
    private void initShaders() {
        if(this.shaderProgramID == -1) {
            this.shaderProgramID = GL20.glCreateProgram();
            int vertexShaderID = -1;
            int fragmentShaderID = -1;
            ResourceLocation[] shaderLocations = this.getShaders();
            try {
                String[] shaders = new String[2];
                for(int i = 0; i < 2; i++) {
                    StringWriter strBuf = new StringWriter();
                    IOUtils.copy(Minecraft.getInstance().getResourceManager().getResource(shaderLocations[i]).getInputStream(), strBuf, "UTF-8");
                    shaders[i] = strBuf.toString();
                }
                vertexShaderID = createShader(shaders[0], ARBVertexShader.GL_VERTEX_SHADER_ARB);
                fragmentShaderID = createShader(shaders[1], ARBFragmentShader.GL_FRAGMENT_SHADER_ARB);
            } catch(Exception ex) {
                this.shaderProgramID = -1;

                throw new RuntimeException(String.format("Error creating shaders %s and %s", shaderLocations[0], shaderLocations[1]), ex);
            }
            if(this.shaderProgramID != -1 && vertexShaderID != -1 && fragmentShaderID != -1) {
                //Attach and link vertex and fragment shader to shader program
                GL20.glAttachShader(this.shaderProgramID, vertexShaderID);
                GL20.glAttachShader(this.shaderProgramID, fragmentShaderID);
                GL20.glLinkProgram(this.shaderProgramID);

                //Check for errors
                if (GL20.glGetProgrami(this.shaderProgramID, ARBShaderObjects.GL_OBJECT_LINK_STATUS_ARB) == GL11.GL_FALSE) {
                    throw new RuntimeException(String.format("Error creating shaders %s and %s: %s", shaderLocations[0], shaderLocations[1], getLogInfoProgram(this.shaderProgramID)));
                }
                GL20.glValidateProgram(this.shaderProgramID);
                //ARBShaderObjects.glValidateProgramARB(this.shaderProgramID);
                if (GL20.glGetProgrami(this.shaderProgramID, ARBShaderObjects.GL_OBJECT_VALIDATE_STATUS_ARB) == GL11.GL_FALSE) {
                    throw new RuntimeException(String.format("Error creating shaders %s and %s: %s", shaderLocations[0], shaderLocations[1], getLogInfoProgram(this.shaderProgramID)));
                }

                //Delete vertex and fragment shader
                GL20.glDeleteShader(vertexShaderID);
                GL20.glDeleteShader(fragmentShaderID);

                //Get uniforms
                this.diffuseSamplerUniformID = GL20.glGetUniformLocation(this.shaderProgramID, "TextureChannel");
                this.texelSizeUniformID = GL20.glGetUniformLocation(this.shaderProgramID, "TextureSize");

                //Unbind shader
                GL20.glUseProgram(0);
            } else {
                if(vertexShaderID != -1)
                    GL20.glDeleteShader(vertexShaderID);
                if(fragmentShaderID != 1)
                    GL20.glDeleteShader(fragmentShaderID);
                if(this.shaderProgramID != -1)
                    GL20.glDeleteProgram(this.shaderProgramID);
            }
        }
    }

    /**
     * Compiles and creates a shader from the shader code.
     * @param shaderCode
     * @param shaderType
     * @return
     * @throws Exception
     */
    private static int createShader(String shaderCode, int shaderType) throws Exception {
        int shader = 0;
        try {
            shader = GL20.glCreateShader(shaderType);
            if(shader == 0) {
                return 0;
            }

            GL20.glShaderSource(shader, shaderCode);
            GL20.glCompileShader(shader);
            if (GL20.glGetShaderi(shader, ARBShaderObjects.GL_OBJECT_COMPILE_STATUS_ARB) == GL11.GL_FALSE) {
                String shaderTypeName = shaderType == ARBVertexShader.GL_VERTEX_SHADER_ARB ? "vertex" : shaderType == ARBFragmentShader.GL_FRAGMENT_SHADER_ARB ? "fragment" : "";
                throw new RuntimeException("Error creating " + shaderTypeName + " shader: " + getLogInfoShader(shader));
            }
            return shader;
        } catch(Exception exc) {
            GL20.glDeleteShader(shader);
            throw exc;
        }
    }
    private static String getLogInfoShader(int obj) {
        return GL20.glGetShaderInfoLog(obj, GL20.glGetShaderi(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }
    private static String getLogInfoProgram(int obj) {
        return GL20.glGetProgramInfoLog(obj, GL20.glGetProgrami(obj, ARBShaderObjects.GL_OBJECT_INFO_LOG_LENGTH_ARB));
    }

    /**
     * Returns the shader code. [0] = vertex shader, [1] = fragment shader
     * @return
     */
    protected abstract ResourceLocation[] getShaders();

    /**
     * Uploads any additional uniforms
     */
    protected void uploadUniforms(float partialTicks) {}

    /**
     * Returns additional stages.
     * <p><b>Note:</b> If additional stages are used a blit buffer is required
     * @return
     */
    protected PostProcessingEffect<?>[] getStages() { return null; }

    /**
     * Used to delete additional things and free memory
     */
    protected void deleteEffect() {}

    /**
     * Used to initialize the effect with additional things such as getting uniform locations.
     * Return false if something during initializiation goes wrong.
     */
    protected boolean initEffect() { return true; }

    /**
     * Called after the shader was rendered to the screen
     */
    protected void postRender(float partialTicks) {}

    protected final void uploadFloat(int uniform, float... values) {
        if(uniform >= 0) {
            switch(values.length) {
                default:
                case 1:
                    this.setFloats(FLOAT_BUFFER_1, values);
                    GL20.glUniform1fv(uniform, FLOAT_BUFFER_1);
                    break;
                case 2:
                    this.setFloats(FLOAT_BUFFER_2, values);
                    GL20.glUniform2fv(uniform, FLOAT_BUFFER_2);
                    break;
                case 3:
                    this.setFloats(FLOAT_BUFFER_3, values);
                    GL20.glUniform3fv(uniform, FLOAT_BUFFER_3);
                    break;
                case 4:
                    this.setFloats(FLOAT_BUFFER_4, values);
                    GL20.glUniform4fv(uniform, FLOAT_BUFFER_4);
                    break;
            }
        }
    }

    private void setFloats(FloatBuffer buffer, float[] values) {
        buffer.position(0);
        for (float value : values) buffer.put(value);
        buffer.flip();
    }

    protected final void uploadInt(int uniform, int... values) {
        if(uniform >= 0) {
            switch(values.length) {
                default:
                case 1:
                    this.setInts(INT_BUFFER_1, values);
                    GL20.glUniform1iv(uniform, INT_BUFFER_1);
                    break;
                case 2:
                    this.setInts(INT_BUFFER_2, values);
                    GL20.glUniform2iv(uniform, INT_BUFFER_2);
                    break;
                case 3:
                    this.setInts(INT_BUFFER_3, values);
                    GL20.glUniform3iv(uniform, INT_BUFFER_3);
                    break;
                case 4:
                    this.setInts(INT_BUFFER_4, values);
                    GL20.glUniform4iv(uniform, INT_BUFFER_4);
                    break;
            }
        }
    }

    private void setInts(IntBuffer buffer, int[] values) {
        buffer.position(0);
        for (int value : values) buffer.put(value);
        buffer.flip();
    }

    protected final void uploadIntArray(int uniform, IntBuffer buffer) {
        if(uniform >= 0) {
            GL20.glUniform1iv(uniform, buffer);
        }
    }

    protected final void uploadFloatArray(int uniform, FloatBuffer buffer) {
        if(uniform >= 0) {
            GL20.glUniform1fv(uniform, buffer);
        }
    }

    /**
     * Uploads a sampler.
     * Texture unit 0 is reserved for the default diffuse sampler
     * @param uniform
     * @param texture
     * @param textureUnit
     */
    protected final void uploadSampler(int uniform, int texture, int textureUnit) {
        if(uniform >= 0 && textureUnit >= 0) {
            RenderSystem.activeTexture('\u84c0' + textureUnit);
            RenderSystem.enableTexture();
            RenderSystem.bindTexture(texture);
            GL20.glUniform1i(uniform, textureUnit);
            RenderSystem.disableTexture();
            RenderSystem.activeTexture('\u84c0');
        }
    }

    /**
     * Uploads a sampler.
     * Texture unit 0 is reserved for the default diffuse sampler
     * @param uniform
     * @param texture
     * @param textureUnit
     */
    protected final void uploadSampler(int uniform, ResourceLocation texture, int textureUnit) {
        if(uniform >= 0 && textureUnit >= 0) {
            RenderSystem.activeTexture('\u84c0' + textureUnit);
            RenderSystem.enableTexture();
            Minecraft.getInstance().textureManager.bind(texture);
            GL20.glUniform1i(uniform, textureUnit);
            RenderSystem.disableTexture();
            RenderSystem.activeTexture('\u84c0');
        }
    }

    /**
     * Uploads a matrix
     * @param uniform
     * @param matrix
     */
    protected final void uploadMatrix4f(int uniform, Matrix4f matrix) {
        if(uniform >= 0) {
            MATRIX4F_BUFFER.position(0);
            MATRIX4F_BUFFER.put(0, matrix.m00);
            MATRIX4F_BUFFER.put(1, matrix.m01);
            MATRIX4F_BUFFER.put(2, matrix.m02);
            MATRIX4F_BUFFER.put(3, matrix.m03);
            MATRIX4F_BUFFER.put(4, matrix.m10);
            MATRIX4F_BUFFER.put(5, matrix.m11);
            MATRIX4F_BUFFER.put(6, matrix.m12);
            MATRIX4F_BUFFER.put(7, matrix.m13);
            MATRIX4F_BUFFER.put(8, matrix.m20);
            MATRIX4F_BUFFER.put(9, matrix.m21);
            MATRIX4F_BUFFER.put(10, matrix.m22);
            MATRIX4F_BUFFER.put(11, matrix.m23);
            MATRIX4F_BUFFER.put(12, matrix.m30);
            MATRIX4F_BUFFER.put(13, matrix.m31);
            MATRIX4F_BUFFER.put(14, matrix.m32);
            MATRIX4F_BUFFER.put(15, matrix.m33);
            GL20.glUniformMatrix4fv(uniform, true, MATRIX4F_BUFFER);
        }
    }

    /**
     * Returns the uniform location
     * @param name
     * @return
     */
    protected final int getUniform(String name) {
        return GL20.glGetUniformLocation(this.getShaderProgram(), name);
    }
}
