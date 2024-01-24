package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ShaderInstance.class)
public interface ShaderInstanceAccessor {
    @Accessor("samplerLocations")
    List<Integer> samplerLocations();
}
