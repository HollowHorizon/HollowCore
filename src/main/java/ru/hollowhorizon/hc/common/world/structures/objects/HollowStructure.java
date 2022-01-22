package ru.hollowhorizon.hc.common.world.structures.objects;

import com.mojang.serialization.Codec;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public abstract class HollowStructure<T extends IFeatureConfig> extends Structure<T> {
    public static boolean isGeneratedABoolean = false;
    private final ResourceLocation structurePath;
    private final GenerationStage.Decoration decorationStage;

    public HollowStructure(Codec<T> codec, GenerationStage.Decoration decorationStage, String templatePoolPath) {
        super(codec);
        this.structurePath = new ResourceLocation(MODID, templatePoolPath);
        this.decorationStage = decorationStage;
    }

    @Override
    public GenerationStage.Decoration step() {
        return decorationStage;
    }

    @Override
    public IStartFactory<T> getStartFactory() {
        return getStructureStart();
    }

    public ResourceLocation getTemplatePoolPath() {
        return this.structurePath;
    }

    protected abstract HollowStartFactory<T> getStructureStart();

    public interface HollowStartFactory<C extends IFeatureConfig> extends Structure.IStartFactory<C> {
        @Override
        default StructureStart<C> create(Structure<C> structure, int chunkX, int chunkZ, MutableBoundingBox boundingBox, int references, long seed) {
            return create((HollowStructure<C>) structure, chunkX, chunkZ, boundingBox, references, seed);
        }

        HollowStructureStart<C> create(HollowStructure<C> structure, int chunkX, int chunkZ, MutableBoundingBox boundingBox, int references, long seed);
    }

}
