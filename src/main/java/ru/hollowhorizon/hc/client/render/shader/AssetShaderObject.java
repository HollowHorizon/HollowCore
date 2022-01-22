package ru.hollowhorizon.hc.client.render.shader;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class AssetShaderObject extends AbstractShaderObject implements ISelectiveResourceReloadListener {

    private final ResourceLocation asset;
    private String source;

    public AssetShaderObject(String name, ShaderType type, Collection<Uniform> uniforms, ResourceLocation asset) {
        super(name, type, uniforms);
        this.asset = Objects.requireNonNull(asset);
    }

    @Override
    protected String getSource() {
        if (source == null) {
            try (IResource resource = Minecraft.getInstance().getResourceManager().getResource(asset)) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
                    source = reader.lines().collect(Collectors.joining("\n"));
                    if(source.contains("#import inputs")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/inputs.hollowlib");
                        source = source.replaceAll("#import inputs", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                    if(source.contains("#import color")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/color.hollowlib");
                        source = source.replaceAll("#import color", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                    if(source.contains("#import effects")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/effects.hollowlib");
                        source = source.replaceAll("#import effects", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                    if(source.contains("#import geometry")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/geometry.hollowlib");
                        source = source.replaceAll("#import geometry", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                    if(source.contains("#import math")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/math.hollowlib");
                        source = source.replaceAll("#import math", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                    if(source.contains("#import noise")) {
                        ResourceLocation inputs = new ResourceLocation(MODID, "shaders/libs/noise.hollowlib");
                        source = source.replaceAll("#import noise", new BufferedReader(new InputStreamReader(Minecraft.getInstance().getResourceManager().getResource(inputs).getInputStream())).lines().collect(Collectors.joining("\n")));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return source;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        source = null;
        dirty = true;
    }
}
