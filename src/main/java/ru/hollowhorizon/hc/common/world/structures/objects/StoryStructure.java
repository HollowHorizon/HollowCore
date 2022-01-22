package ru.hollowhorizon.hc.common.world.structures.objects;

import com.mojang.serialization.Codec;
import net.minecraft.world.gen.GenerationStage;
import ru.hollowhorizon.hc.common.world.structures.config.IntRangeConfig;
import ru.hollowhorizon.hc.common.world.structures.config.StructureNameConfig;

public abstract class StoryStructure extends HollowStructure<StructureNameConfig>{
    public StoryStructure(GenerationStage.Decoration decorationStage, String templatePoolPath) {
        super(StructureNameConfig.CODEC, decorationStage, templatePoolPath);
    }
}
