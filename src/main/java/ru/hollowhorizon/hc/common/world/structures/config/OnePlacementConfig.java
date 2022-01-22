package ru.hollowhorizon.hc.common.world.structures.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Collections;

public class OnePlacementConfig implements IFeatureConfig {
    public static final Codec<OnePlacementConfig> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("position").forGetter(config -> config.name))
            .apply(builder, OnePlacementConfig::new));

    public final String name;

    public OnePlacementConfig(String name) {
        this.name = name;
    }
}
