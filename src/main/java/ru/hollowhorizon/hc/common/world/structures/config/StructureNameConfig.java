package ru.hollowhorizon.hc.common.world.structures.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class StructureNameConfig implements IFeatureConfig {
    public static final Codec<StructureNameConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("structure").forGetter(config -> config.structureName.toString()))
            .apply(builder, (data) -> new StructureNameConfig(new ResourceLocation(data))));
    public final ResourceLocation structureName;

    public StructureNameConfig(ResourceLocation structureName) {
        this.structureName = structureName;
    }
}
