package ru.hollowhorizon.hc.mixins;

import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureBlockEntity.class)
public class StructureBlockMixin {

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"))
    public int read(int p_76125_0_, int p_76125_1_, int p_76125_2_) {
        return Mth.clamp(p_76125_0_, -500, 500);
    }
}
