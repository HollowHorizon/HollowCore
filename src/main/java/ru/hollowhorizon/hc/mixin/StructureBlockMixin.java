package ru.hollowhorizon.hc.mixin;

import net.minecraft.tileentity.StructureBlockTileEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StructureBlockTileEntity.class)
public class StructureBlockMixin {

    @Redirect(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    public int read(int p_76125_0_, int p_76125_1_, int p_76125_2_) {
        return MathHelper.clamp(p_76125_0_, -500, 500);
    }
}
