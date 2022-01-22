package ru.hollowhorizon.hc.mixin;

import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;

import java.util.Map;

@Mixin(FlatGenerationSettings.class)
public class FlatStructuresAccessor {

    @Shadow(remap = false)
    @Final
    private static Map<Structure<?>, StructureFeature<?, ?>> STRUCTURE_FEATURES;

    @Accessor(value = "STRUCTURE_FEATURES")
    public static Map<Structure<?>, StructureFeature<?, ?>> getStructureFeatures() {
        return HollowJavaUtils.fakeInstance();
    }
}
