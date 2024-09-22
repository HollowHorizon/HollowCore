package ru.hollowhorizon.hc.mixins;

import net.minecraft.core.Registry;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.ICapabilityDispatcher;
import ru.hollowhorizon.hc.api.ICapabilityDispatcherKt;
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormatKt;
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.level.LevelEvent;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//? if >=1.20.1 {
/*import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
*///?} else {
import net.minecraft.world.level.storage.WorldData;

import java.util.Map;
//?}

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ICapabilityDispatcher {
    @Unique
    private final List<CapabilityInstance> hollowCore$capabilities = new ArrayList<>();

    @NotNull
    @Override
    public List<CapabilityInstance> getCapabilities() {
        return hollowCore$capabilities;
    }

    @Shadow
    @Final
    protected LevelStorageSource.LevelStorageAccess storageSource;

    //? if >=1.20.1 {
    /*@Shadow
    @Final
    private Map<ResourceKey<Level>, ServerLevel> levels;

    @Shadow
    @Final
    private LayeredRegistryAccess<RegistryLayer> registries;

    @Shadow
    public abstract LayeredRegistryAccess<RegistryLayer> registries();



    @Inject(method = "createLevels", at = @At("TAIL"))
    private void onSave(ChunkProgressListener $$0, CallbackInfo ci) {
        Registry<LevelStem> registry = registries.compositeAccess().registryOrThrow(Registries.LEVEL_STEM);
        for (ResourceKey<LevelStem> key : registry.registryKeySet()) {
            var level = levels.get(key);
            EventBus.post(new LevelEvent.Load(level));
        }
    }
    *///?} else {
    @Shadow @Final protected WorldData worldData;

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void onSave(ChunkProgressListener $$0, CallbackInfo ci) {
        Registry<LevelStem> registry = worldData.worldGenSettings().dimensions();
        for (ResourceKey<LevelStem> key : registry.registryKeySet()) {
            var level = levels.get(key);
            EventBus.post(new LevelEvent.Load(level));
        }
    }
    //?}

    @Inject(method = "loadLevel", at = @At("TAIL"))
    private void onLoad(CallbackInfo ci) {
        ICapabilityDispatcherKt.initialize(this);

        //? if >=1.21 {
        /*var file = storageSource.getLevelDirectory().path().resolve("server_capability.dat").toFile();
        *///?} elif fabric {
        var file = storageSource.getIconFile().get().getParent().resolve("server_capability.dat").toFile();
        //?} else {
        /*var file = storageSource.getWorldDir().resolve(storageSource.getLevelId()).resolve("server_capability.dat").toFile();
        *///?}
        if (file.exists()) {
            try {
                var stream = new FileInputStream(file);
                var tag = NBTFormatKt.loadAsNBT(stream);
                stream.close();
                ICapabilityDispatcherKt.deserializeCapabilities(this, (CompoundTag) tag);
            } catch (IOException e) {
                HollowCore.LOGGER.error("Can't load {}", file.getName(), e);
            }
        }
    }

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void onSave(CallbackInfo ci) {
        //? if >=1.21 {
        /*var file = storageSource.getLevelDirectory().path().resolve("server_capability.dat").toFile();
         *///?} elif fabric {
        var file = storageSource.getIconFile().get().getParent().resolve("server_capability.dat").toFile();
        //?} else {
        /*var file = storageSource.getWorldDir().resolve(storageSource.getLevelId()).resolve("server_capability.dat").toFile();
         *///?}

        try {
            if(!file.exists()) file.createNewFile();
            var output = new FileOutputStream(file);
            var tag = new CompoundTag();
            ICapabilityDispatcherKt.serializeCapabilities(this, tag);
            NBTFormatKt.save(tag, output);
        } catch (IOException e) {
            HollowCore.LOGGER.error("Can't load {}", file.getName(), e);
        }
    }
}
