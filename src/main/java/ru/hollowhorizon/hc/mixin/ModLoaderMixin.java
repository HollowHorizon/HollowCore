package ru.hollowhorizon.hc.mixin;

import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.ModWorkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;

@Mixin(ModLoader.class)
public class ModLoaderMixin {

    @Inject(method = "gatherAndInitializeMods", at = @At("TAIL"), remap = false)
    private void onModLoading(ModWorkManager.DrivenExecutor syncExecutor, Executor parallelExecutor, Runnable periodicTask, CallbackInfo ci) {
    }
}
