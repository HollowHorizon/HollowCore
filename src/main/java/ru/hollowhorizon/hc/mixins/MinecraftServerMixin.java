package ru.hollowhorizon.hc.mixins;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.level.LevelEvent;

//? if >=1.20.1 {
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import java.util.Map;
//?} else {
/*import net.minecraft.world.level.storage.WorldData;

import java.util.Map;
*///?}

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    //? if >=1.20.1 {
    @Shadow
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
    //?} else {
    /*@Shadow @Final protected WorldData worldData;

    @Shadow @Final private Map<ResourceKey<Level>, ServerLevel> levels;

    @Inject(method = "createLevels", at = @At("TAIL"))
    private void onSave(ChunkProgressListener $$0, CallbackInfo ci) {
        Registry<LevelStem> registry = worldData.worldGenSettings().dimensions();
        for (ResourceKey<LevelStem> key : registry.registryKeySet()) {
            var level = levels.get(key);
            EventBus.post(new LevelEvent.Load(level));
        }
    }
    *///?}
}
