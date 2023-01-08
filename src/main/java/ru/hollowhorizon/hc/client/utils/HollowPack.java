package ru.hollowhorizon.hc.client.utils;

import com.google.gson.JsonObject;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.hollowhorizon.hc.HollowCore;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowPack implements IResourcePack {
    public static List<ResourceLocation> genItemModels = new ArrayList<>();
    public static List<ResourceLocation> genBlockData = new ArrayList<>();
    public static List<ResourceLocation> genParticles = new ArrayList<>();
    static HollowPack packInstance;
    public final Map<ResourceLocation, IResourceStreamSupplier> resourceMap = new HashMap<>();
    @NotNull
    public static final Map<String, JsonObject> genSounds = new HashMap<>();

    private void addItemModel(ResourceLocation location) {
        ResourceLocation models_item = new ResourceLocation(location.getNamespace(), "models/item/" + location.getPath() + ".json");
        resourceMap.put(models_item, ofText("{\"parent\":\"item/handheld\",\"textures\":{\"layer0\":\"" + location.getNamespace() + ":items/" + location.getPath() + "\"}}"));
    }

    private void addParticleModel(ResourceLocation location) {
        ResourceLocation particle = new ResourceLocation(location.getNamespace(), "particles/" + location.getPath() + ".json");
        resourceMap.put(particle, ofText("{\"textures\":[\""+location+"\"]}"));
    }

    private void addBlockModel(ResourceLocation location) {
        ResourceLocation blockstate = new ResourceLocation(location.getNamespace(), "blockstates/" + location.getPath() + ".json");
        ResourceLocation model = new ResourceLocation(location.getNamespace(), "models/item/" + location.getPath() + ".json");
        resourceMap.put(blockstate, ofText("{\"variants\":{\"\":{\"model\":\""+location.getNamespace()+":item/"+location.getPath()+"\"}}}"));
        resourceMap.put(model, ofText("{\"parent\":\"block/cube_all\",\"textures\":{\"all\":\""+location.getNamespace()+":blocks/"+location.getPath()+"\"}}"));
    }

    private void addSoundJson(String modid, JsonObject sound) {
        resourceMap.put(new ResourceLocation(modid, "sounds.json"), ofText(sound.toString()));
    }

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

    public void init() {
        for(ResourceLocation location : genItemModels) {
            addItemModel(location);
        }
        for(ResourceLocation location : genParticles) {
            addParticleModel(location);
        }
        for(ResourceLocation location : genBlockData) {
            addBlockModel(location);
        }
        for(Map.Entry<String, JsonObject> sound : genSounds.entrySet()) {
            addSoundJson(sound.getKey(), sound.getValue());
        }
    }

    @Override
    public InputStream getRootResource(String filename) throws IOException {
        throw new FileNotFoundException(filename);
    }

    @Override
    public InputStream getResource(ResourcePackType p_195761_1_, ResourceLocation location) throws IOException {
        try {
            return resourceMap.get(location).create();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            throw e;
        }


    }

    @Override
    public Collection<ResourceLocation> getResources(ResourcePackType p_225637_1_, String p_225637_2_, String p_225637_3_, int p_225637_4_, Predicate<String> p_225637_5_) {
        return Collections.emptyList();
    }

    @Override
    public boolean hasResource(ResourcePackType p_195764_1_, ResourceLocation location) {
        IResourceStreamSupplier s;
        return (s = resourceMap.get(location)) != null && s.exists();

    }

    @Override
    public Set<String> getNamespaces(ResourcePackType p_195759_1_) {
        Set<String> hSet = new HashSet<>();
        for(ResourceLocation data : resourceMap.keySet()) hSet.add(data.getNamespace());
        return hSet;
    }

    @Nullable
    @Override
    public <T> T getMetadataSection(IMetadataSectionSerializer<T> deserializer) throws IOException {
        if (deserializer.getMetadataSectionName().equals("pack")) {
            JsonObject obj = new JsonObject();
            obj.addProperty("pack_format", 6);
            obj.addProperty("description", "Generated resources for HollowCore");
            return deserializer.fromJson(obj);
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
