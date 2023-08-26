package ru.hollowhorizon.hc.client.utils;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class HollowPack implements PackResources {
    @NotNull
    public static final Map<String, JsonObject> genSounds = new HashMap<>();
    public static List<ResourceLocation> genItemModels = new ArrayList<>();
    public static List<ResourceLocation> genBlockData = new ArrayList<>();
    public static List<ResourceLocation> genParticles = new ArrayList<>();
    static HollowPack packInstance;
    public final Map<ResourceLocation, IResourceStreamSupplier> resourceMap = new HashMap<>();

    private static IResourceStreamSupplier ofText(String text) {
        return IResourceStreamSupplier.create(() -> true, () -> new ByteArrayInputStream(text.getBytes()));
    }

    private static IResourceStreamSupplier ofFile(File file) {
        return IResourceStreamSupplier.create(file::isFile, () -> Files.newInputStream(file.toPath()));
    }

    public static HollowPack getPackInstance() {
        if (packInstance == null) packInstance = new HollowPack();
        packInstance.init();
        return packInstance;
    }

    private void addItemModel(ResourceLocation location) {
        ResourceLocation models_item = new ResourceLocation(location.getNamespace(), "models/item/" + location.getPath() + ".json");
        resourceMap.put(models_item, ofText("{\"parent\":\"item/handheld\",\"textures\":{\"layer0\":\"" + location.getNamespace() + ":items/" + location.getPath() + "\"}}"));
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
        for (ResourceLocation location : genItemModels) {
            addItemModel(location);
        }
        for (ResourceLocation location : genParticles) {
            addParticleModel(location);
        }
        for (ResourceLocation location : genBlockData) {
            addBlockModel(location);
        }
        for (Map.Entry<String, JsonObject> sound : genSounds.entrySet()) {
            addSoundJson(sound.getKey(), sound.getValue());
        }

        genItemModels.clear();
        genParticles.clear();
        genBlockData.clear();
        genSounds.clear();
    }

    @Override
    public InputStream getRootResource(String filename) throws IOException {
        throw new FileNotFoundException(filename);
    }

    @Override
    public InputStream getResource(PackType pType, ResourceLocation pLocation) throws IOException {
        try {
            return resourceMap.get(pLocation).create();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            throw e;
        }
    }

    @Override
    public Collection<ResourceLocation> getResources(PackType pType, String pNamespace, String pPath, Predicate<ResourceLocation> pFilter) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasResource(PackType pType, ResourceLocation pLocation) {
        IResourceStreamSupplier s;
        return (s = resourceMap.get(pLocation)) != null && s.exists();
    }

    @Override
    public Set<String> getNamespaces(PackType pType) {
        Set<String> hSet = new HashSet<>();
        for (ResourceLocation data : resourceMap.keySet()) hSet.add(data.getNamespace());
        return hSet;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) throws IOException {
        if (pDeserializer.getMetadataSectionName().equals("pack")) {
            JsonObject obj = new JsonObject();
            obj.addProperty("pack_format", 6);
            obj.addProperty("description", "Generated resources for HollowCore");
            return pDeserializer.fromJson(obj);
        }
        return null;
    }

    @Override
    public String getName() {
        return "Hollow Core Generated Resources";
    }

    @Override
    public void close() {

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
