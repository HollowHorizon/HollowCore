package ru.hollowhorizon.hc.client.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import ru.hollowhorizon.hc.HollowCore;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ShaderProgram {
    private int programID = -1;

    private final ResourceLocation vertexFile;
    private final ResourceLocation fragmentFile;
    private final List<String> variableNames;

    public ShaderProgram(final ResourceLocation vertexFile, final ResourceLocation fragmentFile, final String... variables) throws IOException {
        this(vertexFile, fragmentFile, Arrays.asList(variables));
    }

    public ShaderProgram(final ResourceLocation vertexFile, final ResourceLocation fragmentFile, final List<String> variableNames) throws IOException {
        this.vertexFile = vertexFile;
        this.fragmentFile = fragmentFile;
        this.variableNames = variableNames;

        ShaderManager.INSTANCE.registerShader(this);
    }

    ShaderDeletionHandler init() throws IOException {
        if (programID != -1)
            throw new IllegalStateException("Failed to initialize the shader. It is already initialized.");

        programID = GL21.glCreateProgram();

        final int vertexShaderID;
        if (vertexFile != null)
        {
            vertexShaderID = loadShader(vertexFile, GL20.GL_VERTEX_SHADER);
            if (vertexShaderID == 0)
            {
                throw new IllegalStateException("Failed to initialize the shader. The vertex shader did not compile.");
            }

            GL20.glAttachShader(programID, vertexShaderID);
        }
        else
        {
            vertexShaderID = -1;
        }

        final int fragmentShaderID;
        if (fragmentFile != null)
        {
            fragmentShaderID = loadShader(fragmentFile, GL20.GL_FRAGMENT_SHADER);
            if (fragmentShaderID == 0)
            {
                throw new IllegalStateException("Failed to initialize the shader. The fragment shader did not compile.");
            }

            GL20.glAttachShader(programID, fragmentShaderID);
        }
        else
        {
            fragmentShaderID = -1;
        }

        bindAttributes(variableNames);

        GL20.glLinkProgram(programID);

        return new ShaderDeletionHandler(vertexShaderID, fragmentShaderID, programID);
    }

    protected void storeAllUniformLocations(final Uniform<?>... uniforms){
        for(final Uniform<?> uniform : uniforms){
            uniform.storeUniformLocation(programID);
        }
        GL20.glValidateProgram(programID);
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public int getProgramID()
    {
        return programID;
    }

    private void bindAttributes(final List<String> inVariables){
        for (int i = 0; i < inVariables.size(); i++) {
            GL20.glBindAttribLocation(programID, i, inVariables.get(i));
        }
    }

    private int loadShader(final ResourceLocation file, final int type) throws IOException
    {
        final String shaderSource = readFileAsString(file);
        final int shaderID = GL20.glCreateShader(type);
        if (shaderID == 0)
            return shaderID;

        GL20.glShaderSource(shaderID, shaderSource);
        GL20.glCompileShader(shaderID);
        if (GL20.glGetShaderi(shaderID, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            HollowCore.LOGGER.error(GL20.glGetShaderInfoLog(shaderID, 500));
            HollowCore.LOGGER.error("Could not compile shader "+ file);
            throw new IllegalStateException("Could not load shader.");
        }
        return shaderID;
    }

    private static String readFileAsString(final ResourceLocation filename) throws IOException
    {
        try (InputStream in = Minecraft.getInstance().getResourceManager().getResource(filename).getInputStream()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        }
        catch (FileNotFoundException ex)
        {
            HollowCore.LOGGER.error("Failed to find shader file: " + filename, ex);
            return "";
        }
    }
}
