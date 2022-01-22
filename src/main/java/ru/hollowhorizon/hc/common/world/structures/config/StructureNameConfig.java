package ru.hollowhorizon.hc.common.world.structures.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class StructureNameConfig implements IFeatureConfig {
    public static final Codec<StructureNameConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("structure").forGetter(config -> config.structureName))
            .apply(builder, StructureNameConfig::new));
    public final String structureName;

    public StructureNameConfig(String structureName) {
        this.structureName = structureName;
    }
}
