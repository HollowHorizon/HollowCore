/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class HollowPack implements PackResources {
    @NotNull
    protected static final Map<String, JsonObject> genSounds = new HashMap<>();
    public static List<ResourceLocation> genItemModels = new ArrayList<>();
    public static List<ResourceLocation> genBlockData = new ArrayList<>();
    protected static List<ResourceLocation> genParticles = new ArrayList<>();
    static HollowPack packInstance;
    public final Map<ResourceLocation, IResourceStreamSupplier> resourceMap = new HashMap<>();

    private static IResourceStreamSupplier ofText(String text) {
        return IResourceStreamSupplier.create(() -> true, () -> new ByteArrayInputStream(text.getBytes()));
    }

    public static HollowPack getPackInstance() {
        if (packInstance == null) packInstance = new HollowPack();
        packInstance.init();
        return packInstance;
    }

    public void generatePostShader(ResourceLocation location) {
        resourceMap.put(new ResourceLocation(location.getNamespace(), "shaders/post/" + location.getPath() + ".json"), ofText("{\"targets\": [\"swap\"],\"passes\": [{\"name\": \"" + location + "\",\"intarget\": \"minecraft:main\",\"outtarget\": \"swap\",\"uniforms\": []},{\"name\": \"" + location + "\",\"intarget\": \"swap\",\"outtarget\": \"minecraft:main\",\"uniforms\": []}]}"));
        resourceMap.put(new ResourceLocation(location.getNamespace(), "shaders/program/" + location.getPath() + ".json"), ofText("{\"blend\":{\"func\":\"add\",\"srcrgb\":\"one\",\"dstrgb\":\"zero\"},\"vertex\":\"sobel\",\"fragment\":\"" + location + "\",\"attributes\":[\"Position\"],\"samplers\":[{\"name\":\"DiffuseSampler\"}],\"uniforms\":[{\"name\":\"ProjMat\",\"type\":\"matrix4x4\",\"count\":16,\"values\":[1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0,0.0,0.0,0.0,0.0,1.0]},{\"name\":\"InSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"OutSize\",\"type\":\"float\",\"count\":2,\"values\":[1.0,1.0]},{\"name\":\"Time\",\"type\":\"float\",\"count\":1,\"values\":[0.0]}]}"));
    }

    private void addItemModel(ResourceLocation location) {
        ResourceLocation modelLocation = new ResourceLocation(location.getNamespace(), "models/item/" + location.getPath() + ".json");
        resourceMap.put(modelLocation, ofText("{\"parent\":\"item/handheld\",\"textures\":{\"layer0\":\"" + location.getNamespace() + ":items/" + location.getPath() + "\"}}"));
    }

    private void addParticleModel(ResourceLocation location) {
        ResourceLocation particle = new ResourceLocation(location.getNamespace(), "particles/" + location.getPath() + ".json");
        resourceMap.put(particle, ofText("{\"textures\":[\"" + location + "\"]}"));
    }

    private void addBlockModel(ResourceLocation location) {
        ResourceLocation blockstate = new ResourceLocation(location.getNamespace(), "blockstates/" + location.getPath() + ".json");
        ResourceLocation model = new ResourceLocation(location.getNamespace(), "models/item/" + location.getPath() + ".json");
        resourceMap.put(blockstate, ofText("{\"variants\":{\"\":{\"model\":\"" + location.getNamespace() + ":item/" + location.getPath() + "\"}}}"));
        resourceMap.put(model, ofText("{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\"" + location.getNamespace() + ":blocks/" + location.getPath() + "\"}}"));
    }

    private void addSoundJson(String modid, JsonObject sound) {
        resourceMap.put(new ResourceLocation(modid, "sounds.json"), ofText(sound.toString()));
    }

    public void init() {
        for (ResourceLocation location : genItemModels) addItemModel(location);
        for (ResourceLocation location : genParticles) addParticleModel(location);
        for (ResourceLocation location : genBlockData) addBlockModel(location);
        for (Map.Entry<String, JsonObject> sound : genSounds.entrySet()) addSoundJson(sound.getKey(), sound.getValue());

        genItemModels.clear();
        genParticles.clear();
        genBlockData.clear();
        genSounds.clear();
    }

    @Override
    public InputStream getRootResource(@NotNull String fileName) throws IOException {
        throw new FileNotFoundException(fileName);
    }

    @Override
    public @NotNull InputStream getResource(@NotNull PackType type, @NotNull ResourceLocation pLocation) throws IOException {
        return resourceMap.get(pLocation).create();
    }

    @Override
    public @NotNull Collection<ResourceLocation> getResources(@NotNull PackType pType, @NotNull String pNamespace, @NotNull String pPath, @NotNull Predicate<ResourceLocation> pFilter) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasResource(@NotNull PackType pType, @NotNull ResourceLocation pLocation) {
        IResourceStreamSupplier s;
        return (s = resourceMap.get(pLocation)) != null && s.exists();
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType pType) {
        Set<String> hSet = new HashSet<>();
        for (ResourceLocation data : resourceMap.keySet()) hSet.add(data.getNamespace());
        return hSet;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) {
        if (pDeserializer.getMetadataSectionName().equals("pack")) {
            JsonObject obj = new JsonObject();
            obj.addProperty("pack_format", 6);
            obj.addProperty("description", "Generated resources for HollowCore");
            return pDeserializer.fromJson(obj);
        }
        return null;
    }

    @Override
    public @NotNull String getName() {
        return "Hollow Core Generated Resources";
    }

    @Override
    public void close() {
        // Nothing to close
    }

    public interface IResourceStreamSupplier {
        static IResourceStreamSupplier create(BooleanSupplier exists, IIOSupplier<InputStream> streamable) {
            return new IResourceStreamSupplier() {
                @Override
                public boolean exists() {
                    return exists.getAsBoolean();
                }

                @Override
                public InputStream create() throws IOException {
                    return streamable.get();
                }
            };
        }

        boolean exists();

        InputStream create() throws IOException;
    }

    @FunctionalInterface
    public interface IIOSupplier<T> {
        T get() throws IOException;
    }

}
