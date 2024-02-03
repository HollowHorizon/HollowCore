package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler;

import java.io.IOException;
import java.util.List;

@Mixin(SoundManager.class)
public class SoundManagerMixin {

    @Redirect(
            method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Lnet/minecraft/client/sounds/SoundManager$Preparations;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/ResourceManager;getResources(Lnet/minecraft/resources/ResourceLocation;)Ljava/util/List;")
    )
    public List<Resource> onJsonLoading(ResourceManager instance, ResourceLocation location) throws IOException {
        if (!instance.hasResource(location) && HollowSoundHandler.INSTANCE.getMODS().contains(location.getNamespace())) {
            return List.of(new SimpleResource("Hollow Core Generated Resources", location, HollowSoundHandler.createJson(instance, location.getNamespace()), null));
        } else {
            return instance.getResources(location);
        }
    }
}
