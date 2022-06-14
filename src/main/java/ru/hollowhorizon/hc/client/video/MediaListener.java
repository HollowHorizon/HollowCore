package ru.hollowhorizon.hc.client.video;

import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import ru.hollowhorizon.hc.client.video.media.InputStreamMedia;
import ru.hollowhorizon.hc.client.video.media.MediaEntry;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class MediaListener {
    public static CompletableFuture<Void> reloadResources(IFutureReloadListener.IStage stage, IResourceManager resourceManager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        System.out.println("mediaINIT");
        return CompletableFuture.allOf(
                CompletableFuture
                        .supplyAsync(() -> resourceManager.listResources("media", fileName -> fileName.endsWith(".mp4")), backgroundExecutor)
                        .thenApplyAsync(resources -> {
                            Map<ResourceLocation, MediaEntry> tasks = new HashMap<>();

                            for (ResourceLocation location : resources) {
                                try {
                                    InputStream stream = resourceManager.getResource(location).getInputStream();
                                    tasks.put(location, new MediaEntry(new InputStreamMedia(() -> stream)));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            return tasks;
                        })
                        .thenAcceptAsync(tasks -> {
                            for (Map.Entry<ResourceLocation, MediaEntry> entry : tasks.entrySet()) {
                                entry.getValue().setRegistryName(entry.getKey());
                                //VideoRegistry.VIDEOS.register(entry.getValue());
                            }
                        })
        );
    }

    public static void registerReload() {
        IReloadableResourceManager reloadable = (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        System.out.println("mediaPre");
        //reloadable.registerReloadListener(MediaListener::reloadResources);
    }
}
