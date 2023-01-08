package ru.hollowhorizon.hc.mixin;

import net.minecraft.resources.IAsyncReloader;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.Unit;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler;
import ru.hollowhorizon.hc.common.events.ResourcePackAddEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadableResourceManager.class)
public abstract class SimpleReloadableResourceManagerMixin {
    @Inject(
            method = "createFullReload",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/resources/SimpleReloadableResourceManager;createReload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/List;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/resources/IAsyncReloader;"
            )
    )
    public void injectResourcePacks(Executor p_219537_1_, Executor p_219537_2_, CompletableFuture<Unit> p_219537_3_, List<IResourcePack> p_219537_4_, CallbackInfoReturnable<IAsyncReloader> cir) {
        HollowSoundHandler.reloadResources((IReloadableResourceManager) this);

        ArrayList<IResourcePack> packs = new ArrayList<>();

        HollowCore.onResourcePackAdd(packs);

        if (FMLEnvironment.dist.isClient()) {
            SimpleReloadableResourceManager thisMgr = (SimpleReloadableResourceManager) (Object) this;

            for (IResourcePack pack : packs) {
                thisMgr.add(pack);
            }

            //thisMgr.registerReloadListener(MediaListener::reloadResources);
        }
    }
}
